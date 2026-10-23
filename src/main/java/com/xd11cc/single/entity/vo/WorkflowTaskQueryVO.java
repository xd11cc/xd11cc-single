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
public class WorkflowTaskQueryVO extends BasePageVO {

    private String processDefinitionKey;

    private String title;
}
