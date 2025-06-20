package com.xd11cc.single.entity.vo.base;

import lombok.Data;

/**
 * @Author: xd11cc
 * @Date: 2025/6/13 21:29
 **/
@Data
public class BasePageVO extends BaseQueryVO{

    private Integer pageNo = 1;

    private Integer pageSize = 10;

    private String orderBy;
}
