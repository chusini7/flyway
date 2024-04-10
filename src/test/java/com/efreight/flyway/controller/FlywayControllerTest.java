package com.efreight.flyway.controller;

import com.alibaba.fastjson.JSON;
import com.efreight.flyway.FlywayApplication;
import com.efreight.flyway.config.FlywayConfiguration;
import com.efreight.flyway.entity.EnvironmentConfig;
import com.efreight.flyway.entity.MigrationInfoEntity;
import com.efreight.flyway.holder.FlywayHolder;
import com.efreight.flyway.req.*;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = FlywayApplication.class)
public class FlywayControllerTest extends TestCase {

    @Autowired
    private FlywayController controller;
    @Resource
    private FlywayConfiguration flywayConfiguration;

    private static EnvironmentConfig config = new EnvironmentConfig()
            .setId(1)
            .setEnv("dev")
            .setUrl("jdbc:mysql://172.16.10.119:3307/flyway_db?characterEncoding=utf8&allowMultiQueries=true&useSSL=false")
            .setUser("root")
            .setPassword("eFreight.cn")
            .setCreateTime(LocalDateTime.now());

    @Before
    public void before(){
        Flyway flyway = flywayConfiguration.flyway(config);
        FlywayHolder.setFlyway(flyway);
    }

    @Test
    public void testInfo(){
        // List<MigrationInfoEntity> info = controller.info("dev", "PENDING");
        InfoReq req = new InfoReq();
        req.setState("");
        req.setEnv("dev");
        List<MigrationInfoEntity> info = controller.info(req);
        System.out.println(JSON.toJSONString(info));
    }

    @Test
    public void testGetFileContent() throws Exception {
        FileContentReq req = new FileContentReq();
        req.setScript("versioned/V1.22.0912__add.sql");
        req.setEnv("dev");
        String text = controller.getFileContent(req);
        System.out.println(text);
    }

    @Test
    public void testValidate() {
        ValidateReq req = new ValidateReq();
        req.setScript("versioned/V1.22.0912__add.sql");
        req.setEnv("dev");
        String text = controller.validate(req);
        System.out.println(text);
    }

    @Test
    public void testRepair() {
        RepairReq req = new RepairReq();
        req.setEnv("dev");
        List<String> scriptList = new ArrayList<>();
        scriptList.add("versioned/V1.22.0912__add.sql");
        req.setScriptList(scriptList);
        String text = controller.repair(req);
        System.out.println(text);
    }

    @Test
    public void testDelete() throws SQLException {
        DeleteReq req = new DeleteReq();
        req.setScript("versioned/V1.22.0913__err.sql");
        req.setEnv("dev");
        String delete = controller.delete(req);
        System.out.println(delete);
    }

    @Test
    public void testMigrate() {
        BaseReq req = new BaseReq();
        req.setEnv("dev");
        controller.migrate(req);
    }
}