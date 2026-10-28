package com.xd11cc.single.entity.vo;

import com.xd11cc.single.entity.base.BasePageVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WorkflowInstanceQueryVO extends BasePageVO {

    private String processDefinitionKey;

    private String status;

    private String title;

    /**
     * 发起人ID（管理员筛选时使用，null=不限制）
     */
    private Long starterUserId;
}
