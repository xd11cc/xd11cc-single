package com.xd11cc.single.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
@Data
@ApiModel("发送用户间消息")
public class SystemNoticeSendVO {

    @NotBlank(message = "标题不能为空")
    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("内容")
    private String content;

    @NotEmpty(message = "接收用户不能为空")
    @ApiModelProperty("接收用户id列表")
    private List<Long> receiverIds;
}
