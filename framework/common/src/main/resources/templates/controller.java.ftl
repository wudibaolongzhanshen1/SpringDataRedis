package ${package.Controller};

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ${package.Service}.${table.serviceName};
import jakarta.annotation.Resource;

/**
* <p>
    * ${table.comment!} 前端控制器
    * </p>
*
* @author ${author}
* @since ${date}
*/
@Tag(name = "${table.comment!entity}", description = "管理 ${table.comment!entity} 相关接口")
@RestController
@RequestMapping("/${entity?lower_case}")
public class ${table.controllerName} {

    @Resource
    private ${table.serviceName} ${(table.serviceName)?uncap_first};

}