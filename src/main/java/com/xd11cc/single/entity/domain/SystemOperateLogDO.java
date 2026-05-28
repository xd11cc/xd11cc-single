package com.xd11cc.single.entity.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xd11cc.single.entity.base.BaseTenantDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author xd11cc
 * @date 2026-05-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("system_operate_log")
public class SystemOperateLogDO extends BaseTenantDO {

    private Long id;

    private String module;

    private String operateType;

    private String operateDesc;

    private String method;

    private String requestMethod;

    private String requestUrl;

    private String requestParam;

    private String responseResult;

    private String status;

    private String errorMsg;

    private String operateIp;

    private Long costTime;
}
