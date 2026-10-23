package com.xd11cc.single.entity.vo;

import com.xd11cc.single.entity.base.BasePageVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author xd11cc
 * @date 2026-08-18
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WorkflowDraftQueryVO extends BasePageVO {

    private String name;

    private String key;

    private String category;

    /**
     * 状态：DRAFT-草稿，其余状态均为已部署模型，null-全部
     */
    private String status;
}
