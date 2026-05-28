package com.xd11cc.single.entity.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xd11cc.single.entity.base.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @author xd11cc
 * @date 2026-05-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("system_tenant")
public class SystemTenantDO extends BaseDO {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String domain;

    private String contactName;

    private String contactPhone;

    private Integer accountCount;

    private String status;

    private Date expireTime;
}
