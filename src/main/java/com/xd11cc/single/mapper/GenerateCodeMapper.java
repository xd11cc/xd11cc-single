package com.xd11cc.single.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.xd11cc.single.entity.vo.ColumnInfoVO;
import com.xd11cc.single.entity.vo.TableInfoQueryVO;
import com.xd11cc.single.entity.vo.TableInfoVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author xd11cc
 * @date 2026-04-22 16:32:10
 * @description 用查询information_schema库，直接忽略租户
 */
@InterceptorIgnore(tenantLine = "true")
public interface GenerateCodeMapper {

    List<TableInfoVO> selectList(@Param("data") TableInfoQueryVO tableInfoQueryVO);

    List<ColumnInfoVO> selectByTableName(@Param("tableName") String tableName);
}
