package com.efreight.flyway.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Libiao
 * @date 2022/10/9
 */
@ApiModel("列表参数对象")
@EqualsAndHashCode(callSuper = true)
@Data
public class InfoReq extends BaseReq {

    @ApiModelProperty(name = "state", required = false, example = "PENDING",
            value = "状态：PENDING,APPLIED,FAILED,OUT_OF_ORDER；其他状态如：SUCCESS...等状态参考枚举类-> MigrationState")
    private String state;
}
