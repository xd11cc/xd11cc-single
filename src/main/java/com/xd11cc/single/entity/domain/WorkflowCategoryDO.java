package com.xd11cc.single.entity.domain;

import com.xd11cc.single.entity.base.BaseTenantDO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;

/**
*   @author xd11cc
*   @date 2026-08-21
*/
@Data
@TableName("workflow_category")
@ApiModel(value = "WorkflowCategoryDO", description = "Flowable流程分类")
public class WorkflowCategoryDO extends BaseTenantDO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键id", required = true)
    private Long id;

    @ApiModelProperty(value = "分类编码，租户内唯一，供流程模型引用", required = true)
    private String categoryCode;

    @ApiModelProperty(value = "分类名称", required = true)
    private String categoryName;

    @ApiModelProperty(value = "显示排序，值越小越靠前", required = true)
    private Integer sort;

    @ApiModelProperty(value = "状态：0正常，1停用", required = true)
    private String status;

    @ApiModelProperty(value = "备注", required = false)
    private String remark;

}