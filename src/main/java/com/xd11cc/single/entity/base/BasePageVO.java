package com.xd11cc.single.entity.base;

import lombok.Data;

/**
 * @Author: xd11cc
 * @Date: 2025/6/13 21:29
 **/
@Data
public class BasePageVO extends BaseQueryVO{

    private Integer currentPage = 1;

    private Integer pageSize = 10;

    private String orderBy;
}
