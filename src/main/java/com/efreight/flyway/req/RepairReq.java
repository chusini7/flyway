package com.efreight.flyway.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author Libiao
 * @date 2022/10/9
 */
@ApiModel("修复脚本参数对象")
@EqualsAndHashCode(callSuper = true)
@Data
public class RepairReq extends BaseReq {

    /**
     * 文件script
     */
    @ApiModelProperty(name = "scriptList", value = "脚本相对路径集合", required = true, example = "[versioned/V1.22.0943__add.sql]")
    @NotEmpty(message = "scriptList不能为空！")
    private List<String> scriptList;

}
