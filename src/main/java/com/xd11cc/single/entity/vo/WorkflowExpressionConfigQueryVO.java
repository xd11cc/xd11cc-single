package com.xd11cc.single.entity.vo;

import com.xd11cc.single.entity.base.BasePageVO;
import lombok.Data;

@Data
public class WorkflowExpressionConfigQueryVO extends BasePageVO {
    private String configKey;
    private String configName;
    private String expressionType;
    private String status;
}
