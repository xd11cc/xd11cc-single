${r'<'}?xml version="1.0" encoding="UTF-8" ?${r'>'}
${r'<'}!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd"${r'>'}

<mapper namespace="${mapperPackage}.${changeClassName}Mapper">

    <!-- 基础映射关系 -->
    <resultMap id="BaseResultMap" type="${entityPackage}.${changeClassName}">
        <#if columns??>
        <#list columns as column>
        <result column="${column.columnName}" jdbcType="${column.columnType?upper_case}" property="${column.changeColumnName}"/>
        </#list>
        </#if>
        <result column="create_time" jdbcType="TIMESTAMP" property="createTime"/>
        <result column="create_user_id" jdbcType="BIGINT" property="createUserId"/>
        <result column="update_time" jdbcType="TIMESTAMP" property="updateTime"/>
        <result column="update_user_id" jdbcType="BIGINT" property="updateUserId"/>
        <result column="del_flag" jdbcType="TINYINT" property="delFlag"/>
        <result column="tenant_id" jdbcType="BIGINT" property="tenantId"/>
    </resultMap>

    <!-- 基础列 -->
    <sql id="Base_sql_column">
        <#if columns??>
        <#list columns as column>${column.columnName}<#if column_has_next>, </#if></#list>, create_time, create_user_id, update_time, update_user_id, del_flag, tenant_id
        </#if>
    </sql>
</mapper>