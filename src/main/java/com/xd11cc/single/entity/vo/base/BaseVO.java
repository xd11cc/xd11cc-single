package com.xd11cc.single.entity.vo.base;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: xd11cc
 * @Date: 2025/6/13 23:12
 **/
@Data
public class BaseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long createUserId;

    private Date createTime;

    private Long updateUserId;

    private Date updateTime;

    private boolean delFlag;

    private String remark;
}
