package com.efreight.flyway.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

/**
 * @author Libiao
 * @date 2022/10/14
 */
@ApiModel("插入执行记录参数对象")
@EqualsAndHashCode(callSuper = true)
@Data
public class ExecutedReq extends BaseReq {

    @ApiModelProperty(name = "script", value = "脚本相对路径", required = true, example = "versioned/V1.22.0915__executed.sql")
    @NotBlank(message = "script不能为空！")
    private String script;
}
