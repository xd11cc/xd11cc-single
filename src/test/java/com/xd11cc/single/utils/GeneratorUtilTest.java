package com.xd11cc.single.utils;

import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.entity.dto.GenerateConfigDTO;
import com.xd11cc.single.entity.vo.ColumnInfoVO;
import com.xd11cc.single.entity.vo.PreviewCodeVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeneratorUtilTest {

    // ==================== generatorCode 空参数校验 ====================

    @Test
    void generatorCode_空列列表_抛GENERATE_CODE_ERROR() {
        GenerateConfigDTO config = new GenerateConfigDTO();
        config.setTableName("sys_user");
        config.setEntityPackage("com.xd11cc");
        config.setControllerPackage("com.xd11cc");
        config.setServicePackage("com.xd11cc");
        config.setMapperPackage("com.xd11cc");
        config.setAuthor("xd11cc");
        config.setHasTenant(true);

        assertThatThrownBy(() -> GeneratorUtil.generatorCode(Collections.emptyList(), config))
                .isInstanceOf(ServiceException.class)
                .satisfies(ex -> assertThat(((ServiceException) ex).getErrorCode())
                        .isEqualTo(SystemErrorEnum.GENERATE_CODE_ERROR));
    }

    @Test
    void generatorCode_null列列表_抛GENERATE_CODE_ERROR() {
        assertThatThrownBy(() -> GeneratorUtil.generatorCode(null, buildConfig("sys_user", true)))
                .isInstanceOf(ServiceException.class)
                .satisfies(ex -> assertThat(((ServiceException) ex).getErrorCode())
                        .isEqualTo(SystemErrorEnum.GENERATE_CODE_ERROR));
    }

    // ==================== buildTemplateParams 参数构建逻辑 ====================

    @Test
    void buildTemplateParams_单列非主键_正确构建列参数() throws Exception {
        List<ColumnInfoVO> columns = Collections.singletonList(buildColumn(
                "user_name", "用户名", "", "varchar(64)", "YES", ""));
        GenerateConfigDTO config = buildConfig("sys_user", true);

        Map<String, Object> params = invokeBuildTemplateParams(columns, config);

        assertThat(params.get("tableName")).isEqualTo("sys_user");
        assertThat(params.get("hasTenant")).isEqualTo(Boolean.TRUE);
        assertThat(params.get("hasTimestamp")).isEqualTo(Boolean.FALSE);
        assertThat(params.get("hasBigDecimal")).isEqualTo(Boolean.FALSE);
        assertThat(params.get("exp")).isNull();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> columnList = (List<Map<String, Object>>) params.get("columns");
        assertThat(columnList).hasSize(1);
        assertThat(columnList.get(0).get("columnName")).isEqualTo("user_name");
        assertThat(columnList.get(0).get("changeColumnName")).isEqualTo("userName");
        assertThat(columnList.get(0).get("columnType")).isEqualTo("varchar(64)");
        assertThat(columnList.get(0).get("changeColumnType")).isEqualTo("String");
        assertThat(columnList.get(0).get("autoIncrement")).isNull();
    }

    @Test
    void buildTemplateParams_自增主键_bigint类型_设置priColumnType为Long() throws Exception {
        ColumnInfoVO idCol = buildColumn("id", "主键", "PRI", "bigint", "NO", "auto_increment");
        ColumnInfoVO nameCol = buildColumn("username", "用户名", "", "varchar(64)", "YES", "");
        List<ColumnInfoVO> columns = Arrays.asList(idCol, nameCol);
        GenerateConfigDTO config = buildConfig("sys_user", true);

        Map<String, Object> params = invokeBuildTemplateParams(columns, config);

        assertThat(params.get("priColumnType")).isEqualTo("Long");
        assertThat(params.get("priChangeColName")).isEqualTo("id");
        assertThat(params.get("hasTimestamp")).isEqualTo(Boolean.FALSE);
        assertThat(params.get("hasBigDecimal")).isEqualTo(Boolean.FALSE);
        assertThat(params.get("exp")).isEqualTo(Boolean.TRUE);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> columnList = (List<Map<String, Object>>) params.get("columns");
        assertThat(columnList.get(0).get("autoIncrement")).isEqualTo(Boolean.TRUE);
    }

    @Test
    void buildTemplateParams_timestamp字段_设置hasTimestamp为true() throws Exception {
        ColumnInfoVO col = buildColumn("create_time", "创建时间", "", "datetime", "YES", "");
        GenerateConfigDTO config = buildConfig("sys_user", true);

        Map<String, Object> params = invokeBuildTemplateParams(
                Collections.singletonList(col), config);

        assertThat(params.get("hasTimestamp")).isEqualTo(Boolean.TRUE);
        assertThat(params.get("hasBigDecimal")).isEqualTo(Boolean.FALSE);
    }

    @Test
    void buildTemplateParams_decimal字段_设置hasBigDecimal为true() throws Exception {
        // 使用 "decimal" 匹配 generator.properties 中的键
        ColumnInfoVO col = buildColumn("amount", "金额", "", "decimal", "YES", "");
        GenerateConfigDTO config = buildConfig("sys_user", true);

        Map<String, Object> params = invokeBuildTemplateParams(
                Collections.singletonList(col), config);

        assertThat(params.get("hasBigDecimal")).isEqualTo(Boolean.TRUE);
        assertThat(params.get("hasTimestamp")).isEqualTo(Boolean.FALSE);
    }

    @Test
    void buildTemplateParams_非租户表_hasTenant为false() throws Exception {
        ColumnInfoVO col = buildColumn("dict_label", "字典标签", "PRI", "varchar(100)", "NO", "");
        GenerateConfigDTO config = buildConfig("sys_dict_type", false);

        Map<String, Object> params = invokeBuildTemplateParams(
                Collections.singletonList(col), config);

        assertThat(params.get("hasTenant")).isEqualTo(Boolean.FALSE);
    }

    @Test
    void buildTemplateParams_多列_正确映射每列的类型和名称转换() throws Exception {
        ColumnInfoVO idCol = buildColumn("id", "主键", "PRI", "bigint", "NO", "auto_increment");
        ColumnInfoVO nameCol = buildColumn("user_name", "用户名", "", "varchar(64)", "YES", "");
        ColumnInfoVO timeCol = buildColumn("create_time", "创建时间", "", "datetime", "YES", "");
        List<ColumnInfoVO> columns = Arrays.asList(idCol, nameCol, timeCol);
        GenerateConfigDTO config = buildConfig("sys_user", true);

        Map<String, Object> params = invokeBuildTemplateParams(columns, config);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> columnList = (List<Map<String, Object>>) params.get("columns");
        assertThat(columnList).hasSize(3);
        assertThat(columnList.get(0).get("changeColumnName")).isEqualTo("id");
        assertThat(columnList.get(0).get("changeColumnType")).isEqualTo("Long");
        assertThat(columnList.get(1).get("changeColumnName")).isEqualTo("userName");
        assertThat(columnList.get(1).get("changeColumnType")).isEqualTo("String");
        assertThat(columnList.get(2).get("changeColumnName")).isEqualTo("createTime");
        // datetime → Date（generator.properties 配置），非 LocalDateTime
        assertThat(columnList.get(2).get("changeColumnType")).isEqualTo("Date");
    }

    // ==================== 辅助方法 ====================

    @SuppressWarnings("unchecked")
    private Map<String, Object> invokeBuildTemplateParams(
            List<ColumnInfoVO> columns, GenerateConfigDTO config) throws Exception {
        Method method = GeneratorUtil.class.getDeclaredMethod(
                "buildTemplateParams", List.class, GenerateConfigDTO.class);
        method.setAccessible(true);
        return (Map<String, Object>) method.invoke(null, columns, config);
    }

    private ColumnInfoVO buildColumn(String columnName, String columnComment,
                                     String columnKey, String dataType,
                                     String isNullable, String extra) {
        ColumnInfoVO column = new ColumnInfoVO();
        column.setColumnName(columnName);
        column.setColumnComment(columnComment);
        column.setColumnKey(columnKey);
        column.setDataType(dataType);
        column.setIsNullable(isNullable);
        column.setExtra(extra);
        return column;
    }

    private GenerateConfigDTO buildConfig(String tableName, boolean hasTenant) {
        GenerateConfigDTO config = new GenerateConfigDTO();
        config.setTableName(tableName);
        config.setEntityPackage("com.xd11cc");
        config.setControllerPackage("com.xd11cc");
        config.setServicePackage("com.xd11cc");
        config.setMapperPackage("com.xd11cc");
        config.setAuthor("xd11cc");
        config.setHasTenant(hasTenant);
        return config;
    }
}
