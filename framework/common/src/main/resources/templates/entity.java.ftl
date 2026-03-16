package ${package.Entity};

<#list table.importPackages as pkg>
    import ${pkg};
</#list>
import lombok.*;
import lombok.experimental.Accessors;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
<#if (cfg.useVal!false)>
    import jakarta.validation.constraints.*;
</#if>

/**
* <p>
    * ${table.comment!}
    * </p>
*
* @author ${author}
*/
@Data
<#if (cfg.useBuilder!false)>
@Builder
@NoArgsConstructor
@AllArgsConstructor
</#if>
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "${table.name}", autoResultMap = <#if (cfg.autoMap!false)>true<#else>false</#if>)
public class ${entity} extends ${superEntityClass} {

<#list table.fields as field>
<#-- 过滤掉已经在 BaseDO 中存在的通用字段 -->
    <#if field.propertyName == "createTime" || field.propertyName == "updateTime">
        <#continue>
    </#if>

    <#if field.comment!?length gt 0>
        /**
        * ${field.comment}
        */
    </#if>
<#-- 处理主键与字段注解 -->
    <#if field.keyFlag>
    @com.baomidou.mybatisplus.annotation.TableId(value = "${field.annotationColumnName}", type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    <#else>
    @TableField("${field.annotationColumnName}")
    </#if>
<#-- Jakarta Validation: 根据元数据自动推断 -->
    <#if (cfg.useVal!false) && field.metaInfo??>
        <#if !field.metaInfo.nullable>
            <#if field.propertyType == 'String'>
    @NotBlank(message = "${field.comment!}不能为空")
            <#else>
    @NotNull(message = "${field.comment!}不能为空")
            </#if>
        </#if>
        <#if field.propertyType == 'String' && field.metaInfo.length gt 0>
    @Size(max = ${field.metaInfo.length?c}, message = "${field.comment!}长度超限")
        </#if>
    </#if>
    private ${field.propertyType} ${field.propertyName};
</#list>

}