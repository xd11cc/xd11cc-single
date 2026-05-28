package com.xd11cc.single.entity.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xd11cc
 * @date 2026-05-28
 */
@Data
@TableName("system_login_log")
public class SystemLoginLogDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String loginType;

    private String status;

    private String loginIp;

    private String browser;

    private String os;

    private String msg;

    private Date loginTime;

    private Long tenantId;
}
