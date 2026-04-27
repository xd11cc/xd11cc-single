package ${entityPackage};

<#if hasTenant?? && hasTenant >
import com.xd11cc.single.entity.base.BaseTenantDO;
<#else>
import com.xd11cc.single.entity.base.BaseDO;
</#if>
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Data;
<#if hasTimestamp>
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
</#if>
<#if hasBigDecimal>
import java.math.BigDecimal;
</#if>
import com.baomidou.mybatisplus.annotation.TableName;
<#if exp>
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
</#if>

/**
*   @author ${author}
*   @date ${date}
*/
@Data
@TableName("${tableName}")
<#if tableComment??>
@ApiModel(value = "${changeClassName}DO", description = "${"${tableComment}" ? replace("表", "对象")}")
</#if>
public class ${changeClassName}DO extends <#if hasTenant?? && hasTenant >BaseTenantDO<#else>BaseDO</#if> implements Serializable {
    private static final long serialVersionUID = 1L;

<#if columns??>
    <#list columns as column>
    <#if column.autoIncrement??>
    @TableId(value = "id", type = IdType.AUTO)
    </#if>
    <#if column.columnComment??>
    @ApiModelProperty(value = "${column.columnComment}", <#if column.isNullable == "YES">required = false<#else>required = true</#if>)
    </#if>
<#--    <#if column.changeColumnType == "Date">-->
<#--    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")-->
<#--    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")-->
<#--    </#if>-->
    private ${column.changeColumnType} ${column.changeColumnName};

    </#list>
</#if>
}