package com.xd11cc.single.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.config.properties.TenantIgnoreProperties;
import com.xd11cc.single.entity.dto.GenerateConfigDTO;
import com.xd11cc.single.entity.vo.ColumnInfoVO;
import com.xd11cc.single.entity.vo.PreviewCodeVO;
import com.xd11cc.single.entity.vo.TableInfoQueryVO;
import com.xd11cc.single.entity.vo.TableInfoVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.mapper.GenerateCodeMapper;
import com.xd11cc.single.service.GenerateCodeService;
import com.xd11cc.single.utils.GeneratorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xd11cc
 * @date 2026-04-22 16:30:04
 * @description
 */
@Service
public class GenerateCodeServiceImpl implements GenerateCodeService {

    @Autowired
    private GenerateCodeMapper generateCodeMapper;
    @Autowired
    private TenantIgnoreProperties tenantIgnoreProperties;

    private static final String AUTHOR = "xd11cc";
    private static final String BASE_PACKAGE = "com.xd11cc.single";

    @Override
    public List<TableInfoVO> list(TableInfoQueryVO tableInfoQueryVO) {
        return generateCodeMapper.selectList(tableInfoQueryVO);
    }

    @Override
    public List<PreviewCodeVO> generateCode(String tableName) {
        List<ColumnInfoVO> columnInfoVOS = generateCodeMapper.selectByTableName(tableName);
        if (CollUtil.isEmpty(columnInfoVOS)) {
            throw new ServiceException(SystemErrorEnum.GENERATE_CODE_ERROR);
        }
        GenerateConfigDTO generateConfigDTO = new GenerateConfigDTO();
        boolean hasTenant = !CollUtil.contains(tenantIgnoreProperties.getIgnoreTables(), tableName);
        generateConfigDTO.setAuthor(AUTHOR);
        generateConfigDTO.setEntityPackage(BASE_PACKAGE);
        generateConfigDTO.setControllerPackage(BASE_PACKAGE);
        generateConfigDTO.setServicePackage(BASE_PACKAGE);
        generateConfigDTO.setMapperPackage(BASE_PACKAGE);
        generateConfigDTO.setHasTenant(hasTenant);
        generateConfigDTO.setTableName(tableName);
        return GeneratorUtil.generatorCode(columnInfoVOS, generateConfigDTO);
    }
}
