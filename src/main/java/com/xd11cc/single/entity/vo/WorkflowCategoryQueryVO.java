package com.xd11cc.single.entity.vo;

import com.xd11cc.single.entity.base.BasePageVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("Flowable流程分类查询对象")
public class WorkflowCategoryQueryVO extends BasePageVO {

    @ApiModelProperty("分类编码")
    private String categoryCode;

    @ApiModelProperty("分类名称")
    private String categoryName;

    @ApiModelProperty("状态：0正常，1停用")
    private String status;
}
