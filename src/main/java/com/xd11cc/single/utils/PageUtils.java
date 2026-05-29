package com.xd11cc.single.utils;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xd11cc.single.entity.base.BasePageVO;
import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.base.ResponseVO;

import java.util.List;
import java.util.function.Supplier;

/**
 * @author xd11cc
 * @date 2026-01-22 22:44:20
 */
public class PageUtils {

    public static <R> ResponseVO<PageResult<R>> page(BasePageVO basePageVO, Supplier<List<R>> supplier) {
        try {
            if (StringUtils.isNotBlank(basePageVO.getOrderBy())) {
                PageHelper.startPage(basePageVO.getCurrentPage(), basePageVO.getPageSize(), basePageVO.getOrderBy());
            } else {
                PageHelper.startPage(basePageVO.getCurrentPage(), basePageVO.getPageSize());
            }
            List<R> rows = supplier.get();
            PageInfo<R> pageInfo = new PageInfo<>(rows);
            return ResponseVO.success(new PageResult<>(pageInfo.getList(), pageInfo.getTotal()));
        } finally {
            PageHelper.clearPage();
        }
    }

    public static <R> ResponseVO<PageResult<R>> empty() {
        return ResponseVO.success(PageResult.empty());
    }
}
