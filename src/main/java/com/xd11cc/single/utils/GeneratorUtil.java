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

    public static List<PreviewCodeVO> generatorCode(List<ColumnInfoVO> columnInfos, GenerateConfigDTO generateConfigDTO) {
        List<PreviewCodeVO> previewCodeVOS = new ArrayList<>();
        try {
            // 初始化freemarker配置
            Configuration configuration = new Configuration(Configuration.VERSION_2_3_23);
            configuration.setClassForTemplateLoading(GeneratorUtil.class, "/template");
            configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());

            // 封装模版参数
            Map<String, Object> map = buildTemplateParams(columnInfos, generateConfigDTO);
            String className = ColumnUtil.toCapitalizeCamlCase(generateConfigDTO.getTableName());

            // 渲染所有模版，返回代码
            previewCodeVOS.add(genEntity(configuration, className, map));
            previewCodeVOS.add(genController(configuration, className, map));
            previewCodeVOS.add(genService(configuration, className, map));
            previewCodeVOS.add(genServiceImpl(configuration, className, map));
            previewCodeVOS.add(genMapper(configuration, className, map));
            previewCodeVOS.add(genMapperXml(configuration, className, map));
            return previewCodeVOS;
        }catch (Exception e){
            log.error(e.getMessage(), e);
            throw new ServiceException(SystemErrorEnum.GENERATE_CODE_ERROR);
        }
    }

    /**
     * 生成mapperXml代码
     * @param configuration
     * @param className
     * @param map
     * @return
     */
    private static PreviewCodeVO genMapperXml(Configuration configuration, String className, Map<String, Object> map) {
        String code = renderTemplate(configuration, "mapperXml.ftl", map);
        return new PreviewCodeVO(String.format("%sMapper.xml", className), code);
    }

    /**
     * 生成mapper代码
     * @param configuration
     * @param className
     * @param map
     * @return
     */
    private static PreviewCodeVO genMapper(Configuration configuration, String className, Map<String, Object> map) {
        String code = renderTemplate(configuration, "mapper.ftl", map);
        return new PreviewCodeVO(String.format("%sMapper.java", className), code);
    }

    /**
     * 生成serviceImpl代码
     * @param configuration
     * @param className
     * @param map
     * @return
     */
    private static PreviewCodeVO genServiceImpl(Configuration configuration, String className, Map<String, Object> map) {
        String code = renderTemplate(configuration, "serviceImpl.ftl", map);
        return new PreviewCodeVO(String.format("%sServiceImpl.java", className), code);
    }

    /**
     * 生成service代码
     * @param configuration
     * @param className
     * @param map
     * @return
     */
    private static PreviewCodeVO genService(Configuration configuration, String className, Map<String, Object> map) {
        String code = renderTemplate(configuration, "service.ftl", map);
        return new PreviewCodeVO(String.format("I%sService.java", className), code);
    }

    /**
     * 生成controller代码
     * @param configuration
     * @param className
     * @param map
     * @return
     */
    private static PreviewCodeVO genController(Configuration configuration, String className, Map<String, Object> map) {
        String code = renderTemplate(configuration, "controller.ftl", map);
        return new PreviewCodeVO(String.format("%sController.java", className), code);
    }

    /**
     * 生成实体代码
     * @param configuration
     * @param className
     * @param map
     * @return
     */
    private static PreviewCodeVO genEntity(Configuration configuration, String className, Map<String, Object> map) {
        String code = renderTemplate(configuration, "entity.ftl", map);
        return new PreviewCodeVO(className + ".java", code);
    }

    /**
     * 渲染模版通用方法
     * @param configuration
     * @param templateName
     * @param params
     * @return
     */
    private static String renderTemplate(Configuration configuration, String templateName, Map<String, Object> params) {
        try (StringWriter writer = new StringWriter()){
            Template template = configuration.getTemplate(templateName);
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
