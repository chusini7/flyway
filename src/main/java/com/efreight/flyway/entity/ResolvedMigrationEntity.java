package com.efreight.flyway.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@ApiModel("解析的脚本信息对象")
@Data
@Accessors(chain = true)
public class ResolvedMigrationEntity {

    @ApiModelProperty(name = "version", value = "版本号", required = true, example = "1.22.0911")
    private String version;

    @ApiModelProperty(name = "description", value = "描述", required = true, example = "create table test")
    private String description;

    @ApiModelProperty(name = "type", value = "类型", required = true, example = "SQL")
    private String type;

    @ApiModelProperty(name = "script", value = "脚本相对路径", required = true, example = "versioned/V1.22.0911__create_aaa_test.sql")
    private String script;

    @ApiModelProperty(name = "checksum", value = "文件内容计算值", required = true, example = "504432416")
    private Integer checksum;

    @ApiModelProperty(name = "physicalLocation", value = "脚本绝对路径", required = true, example = "/Users/libiao/Java/efProjects/flyway/target/classes/migration/versioned/V1.22.0911__create_aaa_test.sql")
    private String physicalLocation;
}
