package com.xd11cc.single.entity.vo.base;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: xd11cc
 * @Date: 2025/6/13 21:29
 **/
@Data
public class BaseQueryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 开始时间
     */
    private Date beginTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 数据过滤
     */
    private String dataScope;
}
