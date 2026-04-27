package com.xd11cc.single.service;

import com.xd11cc.single.entity.base.BasePageVO;
import com.xd11cc.single.entity.vo.PreviewCodeVO;
import com.xd11cc.single.entity.vo.TableInfoQueryVO;
import com.xd11cc.single.entity.vo.TableInfoVO;

import java.util.List;

/**
 * @author xd11cc
 * @date 2026-04-22 16:29:57
 * @description
 */
public interface GenerateCodeService {

    List<TableInfoVO> list(TableInfoQueryVO tableInfoQueryVO);

    List<PreviewCodeVO> generateCode(String tableName);
}
