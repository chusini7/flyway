package com.efreight.flyway.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@ApiModel("列表响应对象")
@Data
@Accessors(chain = true)
public class MigrationInfoEntity {

    @ApiModelProperty(name = "installedRank", value = "flyway_schema_history表主键", required = false, example = "1")
    private Integer installedRank;

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

    @ApiModelProperty(name = "installedOn", value = "脚本执行时间", required = true, example = "2022-10-03 08:16:33")
    @JsonFormat(shape = JsonFormat.Shape.STRING , pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Date installedOn;

    @ApiModelProperty(name = "installedBy", value = "执行人", required = true, example = "root")
    private String installedBy;

    @ApiModelProperty(name = "executionTime", value = "执行耗时", required = true, example = "60")
    private Integer executionTime;

    @ApiModelProperty(name = "state", value = "脚本状态", required = true, example = "SUCCESS")
    private String state;

    @ApiModelProperty(name = "resolvedMigrationEntity", value = "解析的脚本信息", required = false)
    private ResolvedMigrationEntity resolvedMigrationEntity;

    @ApiModelProperty(name = "appliedMigrationEntity", value = "已执行脚本信息", required = false)
    private AppliedMigrationEntity appliedMigrationEntity;
}
