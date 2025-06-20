package com.xd11cc.single.entity.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xd11cc.single.entity.vo.base.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: xd11cc
 * @Date: 2025/6/13 23:50
 **/
@Data
@ApiModel("岗位")
@TableName("system_post")
public class SystemPostDO extends BaseVO {

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("岗位编码")
    private String postCode;

    @ApiModelProperty("岗位名称")
    private String postName;

    @ApiModelProperty("岗位状态，字典类型system_enable_status")
    private String status;
}
