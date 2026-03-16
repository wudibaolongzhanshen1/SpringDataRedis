package ${package.Mapper};

import ${package.Entity}.${entity};
import ${superMapperClassPackage};
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
* ${table.comment!} Mapper 接口
*/
@Mapper
public interface ${table.mapperName} extends ${superMapperClass}<${entity}> {

<#list table.fields as field>
<#-- 范式：对唯一标识类字段（ID, Code, Token, Mobile等）生成快捷查询 -->
    <#if field.propertyName?lower_case?ends_with("code")
    || field.propertyName?lower_case?contains("token")
    || field.propertyName?lower_case?contains("account")>

        /**
        * 根据 ${field.comment!field.propertyName} 查询
        */
        default <#if field.propertyName?lower_case?contains("access")>${entity}<#else>List<${entity}></#if> selectBy${field.propertyName?cap_first}(String ${field.propertyName}) {
        <#if field.propertyName?lower_case?contains("access")>
            return selectOne(Wrappers.lambdaQuery(${entity}.class).eq(${entity}::get${field.propertyName?cap_first}, ${field.propertyName}));
        <#else>
            return selectList(Wrappers.lambdaQuery(${entity}.class).eq(${entity}::get${field.propertyName?cap_first}, ${field.propertyName}));
        </#if>
        }
    </#if>
</#list>
}