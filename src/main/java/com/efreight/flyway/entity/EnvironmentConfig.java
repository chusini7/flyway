package com.efreight.flyway.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author Libiao
 * @date 2022/10/8
 */
@Data
@Accessors(chain = true)
public class EnvironmentConfig {

    private Integer id;
    private String env;
    private String url;
    private String user;
    private String password;
    private LocalDateTime createTime;
}
