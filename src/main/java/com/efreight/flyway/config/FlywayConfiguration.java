package com.efreight.flyway.config;

import com.efreight.flyway.entity.EnvironmentConfig;
import com.efreight.flyway.resolver.LocationResolver;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Configuration
@ConditionalOnProperty(prefix = "spring.flyway", name = "enabled")
@EnableConfigurationProperties(FlywayProperties.class)
public class FlywayConfiguration {

    @Resource
    private FlywayProperties properties;
    @Resource
    private ObjectProvider<Callback> callbacks;

    public Flyway flyway(EnvironmentConfig config) {
        FluentConfiguration configuration = new FluentConfiguration();
        DataSource dataSource = configureDataSource(configuration, config);
        checkLocationExists(dataSource);
        configureProperties(configuration);
        configureCallbacks(configuration);
        return configuration.load();
    }

    private DataSource configureDataSource(FluentConfiguration configuration, EnvironmentConfig config) {
        if (Objects.nonNull(config)) {
            configuration.dataSource(config.getUrl(), config.getUser(), config.getPassword());
        } else {
            configuration.dataSource(properties.getUrl(), properties.getUser(), properties.getPassword());
        }
        if (!CollectionUtils.isEmpty(properties.getInitSqls())) {
            String initSql = StringUtils.collectionToDelimitedString(properties.getInitSqls(), "\n");
            configuration.initSql(initSql);
        }
        return configuration.getDataSource();
    }

    private void checkLocationExists(DataSource dataSource) {
        if (properties.isCheckLocation()) {
            String[] locations = new LocationResolver(dataSource).resolveLocations(properties.getLocations());
            Assert.state(locations.length != 0, "Migration script locations not configured");
        }
    }

    private void configureProperties(FluentConfiguration configuration) {
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        String[] locations = new LocationResolver(configuration.getDataSource())
                .resolveLocations(properties.getLocations());
        map.from(locations).to(configuration::locations);
        map.from(properties.getEncoding())
                .to(configuration::encoding);
        map.from(properties.getConnectRetries())
                .to(configuration::connectRetries);
        map.from(properties.getSchemas())
                .as(StringUtils::toStringArray)
                .to(configuration::schemas);
        map.from(properties.getTable())
                .to(configuration::table);
        map.from(properties.getBaselineDescription())
                .to(configuration::baselineDescription);
        map.from(properties.getBaselineVersion())
                .to(configuration::baselineVersion);
        map.from(properties.getInstalledBy())
                .to(configuration::installedBy);
        map.from(properties.getPlaceholders())
                .to(configuration::placeholders);
        map.from(properties.getPlaceholderPrefix())
                .to(configuration::placeholderPrefix);
        map.from(properties.getPlaceholderSuffix())
                .to(configuration::placeholderSuffix);
        map.from(properties.isPlaceholderReplacement())
                .to(configuration::placeholderReplacement);
        map.from(properties.getSqlMigrationPrefix())
                .to(configuration::sqlMigrationPrefix);
        map.from(properties.getSqlMigrationSuffixes())
                .as(StringUtils::toStringArray)
                .to(configuration::sqlMigrationSuffixes);
        map.from(properties.getSqlMigrationSeparator())
                .to(configuration::sqlMigrationSeparator);
        map.from(properties.getRepeatableSqlMigrationPrefix())
                .to(configuration::repeatableSqlMigrationPrefix);
        map.from(properties.getTarget())
                .to(configuration::target);
        map.from(properties.isBaselineOnMigrate())
                .to(configuration::baselineOnMigrate);
        map.from(properties.isCleanDisabled())
                .to(configuration::cleanDisabled);
        map.from(properties.isCleanOnValidationError())
                .to(configuration::cleanOnValidationError);
        map.from(properties.isGroup())
                .to(configuration::group);
        map.from(properties.isIgnoreMissingMigrations())
                .to(configuration::ignoreMissingMigrations);
        map.from(properties.isIgnoreIgnoredMigrations())
                .to(configuration::ignoreIgnoredMigrations);
        map.from(properties.isIgnorePendingMigrations())
                .to(configuration::ignorePendingMigrations);
        map.from(properties.isIgnoreFutureMigrations())
                .to(configuration::ignoreFutureMigrations);
        map.from(properties.isMixed())
                .to(configuration::mixed);
        map.from(properties.isOutOfOrder())
                .to(configuration::outOfOrder);
        map.from(properties.isSkipDefaultCallbacks())
                .to(configuration::skipDefaultCallbacks);
        map.from(properties.isSkipDefaultResolvers())
                .to(configuration::skipDefaultResolvers);
        map.from(properties.isValidateOnMigrate())
                .to(configuration::validateOnMigrate);
    }

    private void configureCallbacks(FluentConfiguration configuration) {
        List<Callback> collect = callbacks.orderedStream()
                .collect(Collectors.toList());
        if (!collect.isEmpty()) {
            configuration.callbacks(collect.toArray(new Callback[0]));
        }
    }

}
