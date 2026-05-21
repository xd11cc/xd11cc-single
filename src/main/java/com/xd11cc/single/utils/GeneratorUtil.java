package com.xd11cc.single.utils;

import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.entity.dto.GenerateConfigDTO;
import com.xd11cc.single.entity.vo.ColumnInfoVO;
import com.xd11cc.single.entity.vo.PreviewCodeVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代码生成
 *
 * @author xd11cc
 * @date 2023/4/25  17:07
 */
@Slf4j
public class GeneratorUtil {

    private static final String TIMESTAMP = "Date";
    private static final String BIGDECIMAL = "BigDecimal";
    private static final String PRI = "PRI";
    private static final String EXTRA = "auto_increment";

    private static final Configuration CONFIGURATION;

    static {
        CONFIGURATION = new Configuration(Configuration.VERSION_2_3_23);
        CONFIGURATION.setClassForTemplateLoading(GeneratorUtil.class, "/template");
        CONFIGURATION.setDefaultEncoding(StandardCharsets.UTF_8.name());
    }

    private static final String[][] TEMPLATES = {
            {"entity.ftl", "%s.java"},
            {"controller.ftl", "%sController.java"},
            {"service.ftl", "I%sService.java"},
            {"serviceImpl.ftl", "%sServiceImpl.java"},
            {"mapper.ftl", "%sMapper.java"},
            {"mapperXml.ftl", "%sMapper.xml"},
    };

    /**
     * 生成代码
     * @param columnInfos
     * @param generateConfigDTO
     * @return
     */
    public static List<PreviewCodeVO> generatorCode(List<ColumnInfoVO> columnInfos, GenerateConfigDTO generateConfigDTO) {
        if (columnInfos == null || columnInfos.isEmpty()) {
            throw new ServiceException(SystemErrorEnum.GENERATE_CODE_ERROR);
        }
        try {
            Map<String, Object> map = buildTemplateParams(columnInfos, generateConfigDTO);
            String className = ColumnUtil.toCapitalizeCamlCase(generateConfigDTO.getTableName());

            List<PreviewCodeVO> previewCodeVOS = new ArrayList<>();
            for (String[] tpl : TEMPLATES) {
                String code = renderTemplate(tpl[0], map);
                previewCodeVOS.add(new PreviewCodeVO(String.format(tpl[1], className), code));
            }
            return previewCodeVOS;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(SystemErrorEnum.GENERATE_CODE_ERROR);
        }
    }

    /**
     * 渲染模版通用方法
     * @param templateName
     * @param params
     * @return
     */
    private static String renderTemplate(String templateName, Map<String, Object> params) {
        try (StringWriter writer = new StringWriter()){
            Template template = CONFIGURATION.getTemplate(templateName);
            template.process(params, writer);
            return writer.toString();
        } catch (IOException | TemplateException e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(SystemErrorEnum.TEMPLATE_LOADING_ERROR);
        }
    }

    /**
     * 封装模版参数
     * @param columnInfos
     * @param generateConfigDTO
     * @return
     */
    private static Map<String, Object> buildTemplateParams(List<ColumnInfoVO> columnInfos, GenerateConfigDTO generateConfigDTO) {
        Map<String, Object> map = new HashMap<>();
        map.put("entityPackage", generateConfigDTO.getEntityPackage());
        map.put("controllerPackage", generateConfigDTO.getControllerPackage());
        map.put("servicePackage", generateConfigDTO.getServicePackage());
        map.put("mapperPackage", generateConfigDTO.getMapperPackage());
        map.put("author", generateConfigDTO.getAuthor());
        map.put("date", LocalDate.now().toString());
        map.put("tableName", generateConfigDTO.getTableName());
        map.put("tableComment", columnInfos.get(0).getTableComment());
        map.put("className", ColumnUtil.toCamelCase(generateConfigDTO.getTableName()));
        map.put("changeClassName", ColumnUtil.toCapitalizeCamlCase(generateConfigDTO.getTableName()));
        map.put("hasTenant", generateConfigDTO.isHasTenant());
        map.put("hasTimestamp", Boolean.FALSE);
        map.put("hasBigDecimal", Boolean.FALSE);
        List<Map<String, Object>> columns = new ArrayList<>();
        columnInfos.forEach(columnInfo -> {
            Map<String, Object> column = new HashMap<>();
            column.put("columnComment", columnInfo.getColumnComment());
            column.put("columnKey", columnInfo.getColumnKey());

            String colType = ColumnUtil.cloToJava(columnInfo.getDataType());
            String changeColumnName = ColumnUtil.toCamelCase(columnInfo.getColumnName());
            if (PRI.equals(columnInfo.getColumnKey())){
                map.put("priColumnType", colType);
                map.put("priChangeColName", changeColumnName);
            }
            if (TIMESTAMP.equals(colType)){
                map.put("hasTimestamp", Boolean.TRUE);
            }
            if (BIGDECIMAL.equals(colType)){
                map.put("hasBigDecimal", Boolean.TRUE);
            }
            column.put("columnType", columnInfo.getDataType());
            column.put("changeColumnType", colType);
            column.put("columnName", columnInfo.getColumnName());
            column.put("isNullable", columnInfo.getIsNullable());
            column.put("changeColumnName", changeColumnName);
            if (EXTRA.equals(columnInfo.getExtra())){
                column.put("autoIncrement", Boolean.TRUE);
                map.put("exp", Boolean.TRUE);
            }
            columns.add(column);
        });
        map.put("columns", columns);
        return map;
    }
}
