package com.xd11cc.single.entity.domain;

import com.xd11cc.single.entity.base.BaseTenantDO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Data;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;

/**
*   @author xd11cc
*   @date 2026-08-13
*/
@Data
@TableName("workflow_instance")
@ApiModel(value = "WorkflowInstanceDO", description = "工作流实例业务关联对象")
public class WorkflowInstanceDO extends BaseTenantDO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID", required = true)
    private Long id;

    @ApiModelProperty(value = "流程定义Key", required = true)
    private String processDefinitionKey;

    @ApiModelProperty(value = "Flowable流程定义ID", required = true)
    private String processDefinitionId;

    @ApiModelProperty(value = "Flowable流程实例ID", required = true)
    private String processInstanceId;

    @ApiModelProperty(value = "业务标识，格式建议为业务类型:业务ID", required = true)
    private String businessKey;

    @ApiModelProperty(value = "流程标题", required = false)
    private String title;

    @ApiModelProperty(value = "状态：0-进行中，1-已完成，2-已取消，3-已驳回", required = true)
    private String status;

    @ApiModelProperty(value = "发起人ID", required = true)
    private Long starterUserId;

    @ApiModelProperty(value = "发起人用户名", required = false)
    private String starterUsername;

    @ApiModelProperty(value = "业务流程变量JSON快照", required = false)
    private String variables;

    @ApiModelProperty(value = "流程结束时间", required = false)
    private Date endTime;

}