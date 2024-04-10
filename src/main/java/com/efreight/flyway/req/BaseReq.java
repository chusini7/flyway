package com.efreight.flyway.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Libiao
 * @date 2022/10/9
 */
@ApiModel("环境参数对象")
@Data
public class BaseReq {

    /**
     * 环境
     */
    @ApiModelProperty(name = "env", value = "环境名称：dev,test,prod", required = true, example = "dev")
    private String env;
}
