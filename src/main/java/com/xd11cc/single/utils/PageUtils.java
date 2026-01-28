package com.xd11cc.single.utils;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xd11cc.single.entity.base.BasePageVO;
import com.xd11cc.single.entity.base.ResponseVO;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author xd11cc
 * @date 2026-01-22 22:44:20
 */
public class PageUtils {

    /**
     * 开始分页
     * @param pageNo
     * @param pageSize
     */
    public static void startPage(Integer pageNo, Integer pageSize){
        PageHelper.startPage(pageNo, pageSize);
    }

    /**
     * 响应请求分页数据
     * @param list
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static ResponseVO getDataTable(List<?> list){
        Map<String, Object> map = new HashMap<>();
        map.put("rows", list);
        map.put("total", new PageInfo<>(list).getTotal());
        map.put("msg", "查询成功");
        return ResponseVO.success(map);
    }

    /**
     * 分页
     * @param basePageVO
     * @param supplier
     * @return
     * @param <R>
     */
    public static <R> ResponseVO page(BasePageVO basePageVO, Supplier<List<R>> supplier){
        try {
            Page<R> p = null;
            if (StringUtils.isNoneBlank(basePageVO.getOrderBy())){
                p = PageHelper.startPage(basePageVO.getCurrentPage(), basePageVO.getPageSize(), basePageVO.getOrderBy());
            }else {
                p = PageHelper.startPage(basePageVO.getCurrentPage(), basePageVO.getPageSize());
            }
            List<R> r = supplier.get();
            PageInfo<R> of = PageInfo.of(p);
            of.setList(r);
            Map<String, Object> map = new HashMap<>();
            map.put("rows", of.getList());
            map.put("total", of.getTotal());
            map.put("msg", "查询成功");
            return ResponseVO.success(map);
        }finally {
            PageHelper.clearPage();
        }
    }

    /**
     * 构造空数据
     * @return
     */
    public static ResponseVO<?> empty(){
        Map<String, Object> map = new HashMap<>();
        map.put("rows", Collections.emptyList());
        map.put("total", 0);
        map.put("msg", "查询成功");
        return ResponseVO.success(map);
    }
}
