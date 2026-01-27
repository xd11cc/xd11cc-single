package com.xd11cc.single.entity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xd11cc
 * @date 2026-01-26 14:34:11
 * @description
 */
@Data
public class TenantDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 租户ID
     */
    private Long id;

    /**
     * 租户名
     */
    private String name;

    /**
     * 租户状态
     */
    private Integer status;

    /**
     * 绑定的域名
     */
    private String domain;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 账户数量
     */
    private Integer accountCount;
}
