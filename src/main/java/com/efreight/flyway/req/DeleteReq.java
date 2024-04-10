package com.efreight.flyway.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

/**
 * @author Libiao
 * @date 2022/10/9
 */
@ApiModel("删除错误执行记录参数对象")
@EqualsAndHashCode(callSuper = true)
@Data
public class DeleteReq extends BaseReq {

    /**
     * 文件script
     */
    @ApiModelProperty(name = "script", value = "脚本相对路径", required = true, example = "versioned/V1.22.0913__err.sql")
    @NotBlank(message = "script不能为空！")
    private String script;

}
