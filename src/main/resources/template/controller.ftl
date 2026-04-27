package ${controllerPackage};

import ${servicePackage}.I${changeClassName}Service;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
* @author ${author}
* @date ${date}
*/
@RestController
@RequestMapping("/${"${tableName}" ? replace("_", "/")}")
@Api(tags = "${"${tableComment}" ? replace("表", "管理")}")
public class ${changeClassName}Controller {

    @Autowired
    private I${changeClassName}Service ${className}Service;

}