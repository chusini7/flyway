package com.efreight.flyway.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Libiao
 * @date 2022/10/9
 */
@ApiModel("校验脚本参数对象")
@EqualsAndHashCode(callSuper = true)
@Data
public class ValidateReq extends BaseReq {

    /**
     * 文件script
     */
    @ApiModelProperty(name = "script", value = "脚本相对路径", required = false, example = "versioned/V1.22.0943__add.sql")
    private String script;

}
