package com.efreight.flyway.controller;

import com.alibaba.fastjson.JSON;
import com.efreight.flyway.entity.AppliedMigrationEntity;
import com.efreight.flyway.entity.MigrationInfoEntity;
import com.efreight.flyway.entity.ResolvedMigrationEntity;
import com.efreight.flyway.holder.FlywayHolder;
import com.efreight.flyway.req.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.*;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.clazz.ClassProvider;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.database.DatabaseFactory;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.info.MigrationInfoImpl;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.TransactionTemplate;
import org.flywaydb.core.internal.resolver.CompositeMigrationResolver;
import org.flywaydb.core.internal.resource.ResourceProvider;
import org.flywaydb.core.internal.scanner.Scanner;
import org.flywaydb.core.internal.schemahistory.AppliedMigration;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.schemahistory.SchemaHistoryFactory;
import org.flywaydb.core.internal.sqlscript.SqlStatementBuilderFactory;
import org.flywaydb.core.internal.util.ObjectUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Api(value = "flyway接口", tags = {"flyway接口"})
@Slf4j
@RestController
@RequestMapping("/flyway")
public class FlywayController {

    private static final String FILE_NOT_FOUND = "文件不存在！";
    private static final String VALIDATE_PASSED = "校验通过！";
    private static final String SUCCESS = "SUCCESS";
    private static final String FAIL = "FAIL";

    /**
     * 列表查询
     */
    @ApiOperation("列表查询接口")
    @GetMapping("/info")
    public List<MigrationInfoEntity> info(@Validated InfoReq req) {
        String state = req.getState();
        MigrationInfoServiceImpl info = (MigrationInfoServiceImpl) FlywayHolder.getFlyway().info();
        MigrationInfo[] infos = info.all();
        if (StringUtils.hasText(state)) {
            switch (state) {
                case "PENDING":
                    infos = info.pending();
                    break;
                case "APPLIED":
                    infos = info.applied();
                    break;
                case "FAILED":
                    infos = info.failed();
                    break;
                case "OUT_OF_ORDER":
                    infos = info.outOfOrder();
                    break;
                default:
                    return Arrays.stream(infos)
                            .filter(x -> MigrationState.valueOf(state) == x.getState())
                            .map(FlywayController::infoToEntity)
                            .collect(Collectors.toList());
            }
        }
        return Arrays.stream(infos)
                .map(FlywayController::infoToEntity)
                .collect(Collectors.toList());
    }

    private static MigrationInfoEntity infoToEntity(MigrationInfo info) {
        MigrationInfoEntity entity = new MigrationInfoEntity()
                .setInstalledRank(info.getInstalledRank())
                .setVersion(Objects.nonNull(info.getVersion()) ? info.getVersion().getVersion() : null)
                .setDescription(info.getDescription())
                .setType(info.getType().name())
                .setScript(info.getScript())
                .setChecksum(info.getChecksum())
                .setInstalledOn(info.getInstalledOn())
                .setInstalledBy(info.getInstalledBy())
                .setExecutionTime(info.getExecutionTime())
                .setState(info.getState().name());
        if (info instanceof MigrationInfoImpl) {
            MigrationInfoImpl infoImpl = (MigrationInfoImpl) info;
            ResolvedMigration resolved = infoImpl.getResolvedMigration();
            if (Objects.nonNull(resolved)) {
                ResolvedMigrationEntity resolvedEntity = new ResolvedMigrationEntity()
                        .setVersion(Objects.nonNull(resolved.getVersion()) ? resolved.getVersion().getVersion() : null)
                        .setDescription(resolved.getDescription())
                        .setScript(resolved.getScript())
                        .setChecksum(resolved.getChecksum())
                        .setType(resolved.getType().name())
                        .setPhysicalLocation(resolved.getPhysicalLocation());
                entity.setResolvedMigrationEntity(resolvedEntity);
            }

            AppliedMigration applied = infoImpl.getAppliedMigration();
            if (Objects.nonNull(applied)) {
                AppliedMigrationEntity appliedEntity = new AppliedMigrationEntity()
                        .setInstalledRank(applied.getInstalledRank())
                        .setVersion(Objects.nonNull(applied.getVersion()) ? applied.getVersion().getVersion() : null)
                        .setDescription(applied.getDescription())
                        .setType(applied.getType().name())
                        .setScript(applied.getScript())
                        .setChecksum(applied.getChecksum())
                        .setInstalledOn(applied.getInstalledOn())
                        .setInstalledBy(applied.getInstalledBy())
                        .setExecutionTime(applied.getExecutionTime())
                        .setSuccess(applied.isSuccess());
                entity.setAppliedMigrationEntity(appliedEntity);
            }
        }
        return entity;
    }

    /**
     * 校验<br/>
     * script传参校验单个，不传校验所有
     *
     * @return 校验结果
     */
    @ApiOperation("校验脚本接口：script不传校验所有")
    @PostMapping("/validate")
    public String validate(@Validated @RequestBody ValidateReq req) {
        Configuration configuration = FlywayHolder.getFlyway().getConfiguration();
        MigrationInfoServiceImpl infoService = buildMigrationInfoServiceImpl(configuration, true);
        if (StringUtils.hasText(req.getScript())) {
            Optional<MigrationInfo> optional = Arrays.stream(infoService.all())
                    .filter(x -> req.getScript().equals(x.getScript()))
                    .findFirst();

            if (optional.isPresent()) {
                String message = ((MigrationInfoImpl) optional.get()).validate();
                return transferValidateMessage(message);
            }
            return FILE_NOT_FOUND;
        }
        String message = infoService.validate();
        return transferValidateMessage(message);
    }

    @SuppressWarnings("rawtypes")
    private static MigrationInfoServiceImpl buildMigrationInfoServiceImpl(Configuration configuration,
                                                                          boolean pending) {
        MigrationInfoServiceImpl infoService;
        Database database = null;
        try {
            database = DatabaseFactory.createDatabase(configuration, true);
            Schema[] schemas = prepareSchemas(database, configuration);
            Scanner scanner = new Scanner(Arrays.asList(configuration.getLocations()),
                    configuration.getClassLoader(), configuration.getEncoding());
            SchemaHistory schemaHistory = SchemaHistoryFactory.getSchemaHistory(configuration, database, schemas[0]);
            MigrationResolver migrationResolver = createMigrationResolver(database, scanner, scanner,
                    database.createSqlStatementBuilderFactory(), configuration);
            infoService = new MigrationInfoServiceImpl(migrationResolver, schemaHistory,
                    configuration, configuration.getTarget(), configuration.isOutOfOrder(), pending,
                    configuration.isIgnoreMissingMigrations(), configuration.isIgnoreIgnoredMigrations(),
                    configuration.isIgnoreFutureMigrations());
            infoService.refresh();
        } finally {
            if (Objects.nonNull(database)) {
                database.close();
            }
        }
        return infoService;
    }

    @SuppressWarnings("rawtypes")
    private static MigrationResolver createMigrationResolver(Database database,
                                                             ResourceProvider resourceProvider,
                                                             ClassProvider classProvider,
                                                             SqlStatementBuilderFactory sqlStatementBuilderFactory,
                                                             Configuration configuration) {

        for (MigrationResolver resolver : configuration.getResolvers()) {
            ConfigUtils.injectFlywayConfiguration(resolver, configuration);
        }

        return new CompositeMigrationResolver(database, resourceProvider, classProvider,
                configuration, sqlStatementBuilderFactory, configuration.getResolvers());
    }

    @SuppressWarnings("rawtypes")
    private static Schema[] prepareSchemas(Database database, Configuration configuration) {
        String[] schemaNames = configuration.getSchemas();
        if (schemaNames.length == 0) {
            Schema currentSchema = database.getMainConnection().getCurrentSchema();
            if (currentSchema == null) {
                throw new FlywayException("Unable to determine schema for the schema history table. Set a default schema for the connection or specify one using the schemas property!");
            }
            schemaNames = new String[]{currentSchema.getName()};
        }
        if (schemaNames.length == 1) {
            log.debug("Schema: " + schemaNames[0]);
        } else {
            log.debug("Schemas: " + org.flywaydb.core.internal.util.StringUtils.arrayToCommaDelimitedString(schemaNames));
        }
        Schema[] schemas = new Schema[schemaNames.length];
        for (int i = 0; i < schemaNames.length; ++i) {
            schemas[i] = database.getMainConnection().getSchema(schemaNames[i]);
        }
        return schemas;
    }

    private static String transferValidateMessage(String validateMessage) {
        return validateMessage == null ? VALIDATE_PASSED : validateMessage;
    }

    /**
     * 修复数据<br/>
     * 修复后的数据都是执行成功状态
     *
     * @return 修复结果
     */
    @SuppressWarnings("rawtypes")
    @ApiOperation("修复错误记录接口(versioned可以修复，procedures若执行错误删除错误记录重新执行)")
    @PostMapping("/repair")
    public String repair(@Validated @RequestBody RepairReq req) {
        if (!CollectionUtils.isEmpty(req.getScriptList())) {
            MigrationInfoServiceImpl infoService = (MigrationInfoServiceImpl) FlywayHolder.getFlyway().info();
            List<MigrationInfo> repairList = Arrays.stream(infoService.all())
                    .filter(x -> req.getScriptList().contains(x.getScript()))
                    .collect(Collectors.toList());
            String message = checkRepair(repairList, req.getScriptList());
            if (StringUtils.hasText(message)) {
                return message;
            }
            Configuration configuration = FlywayHolder.getFlyway().getConfiguration();
            Database database = null;
            String result;
            try {
                database = DatabaseFactory.createDatabase(configuration, true);
                JdbcTemplate jdbcTemplate = database.getMainConnection().getJdbcTemplate();
                TransactionTemplate transactionTemplate = new TransactionTemplate(database.getMainConnection().getJdbcConnection());
                Database finalDatabase = database;
                result = transactionTemplate.execute(() -> {
                    for (MigrationInfo info : repairList) {
                        MigrationInfoImpl infoImpl = (MigrationInfoImpl) info;
                        ResolvedMigration resolved = infoImpl.getResolvedMigration();
                        AppliedMigration applied = infoImpl.getAppliedMigration();

                        MigrationType type = applied.getType().isSynthetic() ? applied.getType() : resolved.getType();
                        log.info("Repairing Schema History table for" +
                                " (Version: " + resolved.getVersion() +
                                ", Description: " + resolved.getDescription() +
                                ", Type: " + type +
                                ", Checksum: " + resolved.getChecksum() +
                                ", Success: true) ");
                        jdbcTemplate.update("UPDATE " + configuration.getTable() + " SET " +
                                        finalDatabase.quote("description") + "=? , " +
                                        finalDatabase.quote("type") + "=? , " +
                                        finalDatabase.quote("success") + "=1 , " +
                                        finalDatabase.quote("checksum") + "=? WHERE " +
                                        finalDatabase.quote("version") + "=?",
                                resolved.getDescription(), type, resolved.getChecksum(), resolved.getVersion());
                    }
                    return SUCCESS;
                });
            } finally {
                if (Objects.nonNull(database)) {
                    database.close();
                }
            }
            return result;
        }
        return FAIL;
    }

    private static String checkRepair(List<MigrationInfo> repairList, List<String> scriptList) {
        if (repairList.size() != scriptList.size()) {
            List<String> findList = repairList.stream()
                    .map(MigrationInfo::getScript)
                    .collect(Collectors.toList());
            scriptList.removeAll(findList);
            return "script: " + JSON.toJSON(scriptList) + "不存在！";
        }
        StringBuilder builder = new StringBuilder();
        for (MigrationInfo info : repairList) {
            MigrationInfoImpl infoImpl = (MigrationInfoImpl) info;
            ResolvedMigration resolved = infoImpl.getResolvedMigration();
            AppliedMigration applied = infoImpl.getAppliedMigration();
            if (!(resolved != null && resolved.getVersion() != null
                    && applied != null && !applied.getType().isSynthetic()
                    && updateNeeded(resolved, applied))) {

                builder.append("script: ");
                builder.append(infoImpl.getScript());
                builder.append(" 不满足修复条件; ");
            }
        }
        return builder.toString();
    }

    private static boolean updateNeeded(ResolvedMigration resolved, AppliedMigration applied) {
        return checksumUpdateNeeded(resolved, applied)
                || descriptionUpdateNeeded(resolved, applied)
                || typeUpdateNeeded(resolved, applied)
                || successNeeded(applied);
    }

    private static boolean checksumUpdateNeeded(ResolvedMigration resolved, AppliedMigration applied) {
        return !ObjectUtils.nullSafeEquals(resolved.getChecksum(), applied.getChecksum());
    }

    private static boolean descriptionUpdateNeeded(ResolvedMigration resolved, AppliedMigration applied) {
        return !ObjectUtils.nullSafeEquals(resolved.getDescription(), applied.getDescription());
    }

    private static boolean typeUpdateNeeded(ResolvedMigration resolved, AppliedMigration applied) {
        return !ObjectUtils.nullSafeEquals(resolved.getType(), applied.getType());
    }

    private static boolean successNeeded(AppliedMigration applied) {
        return !applied.isSuccess();
    }

    /**
     * 脚本数据库已执行，flyway无需执行（需要插入执行成功记录，只有PENDING才可以执行）
     *
     * @return 执行结果
     */
    @SuppressWarnings("rawtypes")
    @ApiOperation("数据库脚本已执行，flyway无需再次执行（需要插入执行成功记录，只有“PENDING”且“版本追踪的”才可以执行）")
    @PutMapping("/executed")
    public String executed(@Validated @RequestBody ExecutedReq req) {
        String script = req.getScript();
        if (StringUtils.hasText(script)) {
            MigrationInfoServiceImpl infoService = (MigrationInfoServiceImpl) FlywayHolder.getFlyway().info();
            Optional<MigrationInfoImpl> optional = Arrays.stream(infoService.pending())
                    .filter(x -> Objects.nonNull(x.getVersion()) && script.equals(x.getScript()))
                    .findFirst();
            if (optional.isPresent()) {
                MigrationInfoImpl infoImpl = optional.get();
                ResolvedMigration resolved = infoImpl.getResolvedMigration();
                Configuration configuration = FlywayHolder.getFlyway().getConfiguration();
                Database database = null;
                try {
                    database = DatabaseFactory.createDatabase(configuration, true);
                    Schema[] schemas = prepareSchemas(database, configuration);
                    SchemaHistory schemaHistory = SchemaHistoryFactory.getSchemaHistory(configuration, database, schemas[0]);
                    log.info("INSERT Schema History table for" +
                            " (Version: " + resolved.getVersion() +
                            ", Description: " + resolved.getDescription() +
                            ", Type: " + resolved.getType() +
                            ", Script: " + resolved.getScript() +
                            ", Checksum: " + resolved.getChecksum() +
                            ", ExecutionTime: 0" +
                            ", Success: true) ");
                    schemaHistory.addAppliedMigration(resolved.getVersion(), resolved.getDescription(),
                            resolved.getType(), resolved.getScript(), resolved.getChecksum(), 0, true);
                } finally {
                    if (Objects.nonNull(database)) {
                        database.close();
                    }
                }
                return SUCCESS;
            }
        }
        return FAIL;
    }

    /**
     * 删除错误记录(只能删除执行失败的记录)
     *
     * @return 修复结果
     */
    @SuppressWarnings("rawtypes")
    @ApiOperation("删除错误记录接口(只能删除执行失败的记录)")
    @DeleteMapping("/delete")
    public String delete(@Validated @RequestBody DeleteReq req) throws SQLException {
        String script = req.getScript();
        if (StringUtils.hasText(script)) {
            MigrationInfoServiceImpl infoService = (MigrationInfoServiceImpl) FlywayHolder.getFlyway().info();
            Optional<MigrationInfo> optional = Arrays.stream(infoService.failed())
                    .filter(x -> script.equals(x.getScript()))
                    .findFirst();
            if (optional.isPresent()) {
                MigrationInfoImpl info = ((MigrationInfoImpl) optional.get());
                AppliedMigration applied = info.getAppliedMigration();
                if (Objects.nonNull(applied) && !applied.isSuccess()) {
                    Configuration configuration = FlywayHolder.getFlyway().getConfiguration();
                    Database database = DatabaseFactory.createDatabase(configuration, true);
                    JdbcTemplate jdbcTemplate = database.getMainConnection().getJdbcTemplate();
                    log.info("DELETE Schema History table for installed_rank = " + applied.getInstalledRank());
                    jdbcTemplate.execute("DELETE FROM " + configuration.getTable() + " WHERE installed_rank = ?", applied.getInstalledRank());
                    return SUCCESS;
                }
            }
        }
        return FAIL;
    }

    /**
     * 获取文件内容
     *
     * @return 文件内容
     */
    @ApiOperation("获取文件内容接口")
    @GetMapping("/file")
    public String getFileContent(@Validated FileContentReq req) throws IOException {
        Location[] locations = FlywayHolder.getFlyway().getConfiguration().getLocations();
        for (Location location : locations) {
            String relativePath = location.getPath() + File.separator + req.getScript();
            ClassPathResource resource = new ClassPathResource(relativePath);
            if (!resource.exists()) {
                continue;
            }
            StringBuilder sb = new StringBuilder();
            try (InputStreamReader isr = new InputStreamReader(resource.getInputStream());
                 BufferedReader reader = new BufferedReader(isr)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                }
            }
            return sb.toString();
        }
        return FILE_NOT_FOUND;
    }

    /**
     * 按环境区分
     */
    @ApiOperation("脚本执行接口")
    @PostMapping("/migrate")
    public String migrate(@Validated @RequestBody BaseReq req) {
        FlywayHolder.getFlyway().migrate();
        return SUCCESS;
    }
}
