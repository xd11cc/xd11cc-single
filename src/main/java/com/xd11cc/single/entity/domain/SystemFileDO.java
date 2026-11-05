package com.xd11cc.single.entity.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xd11cc.single.entity.base.BaseTenantDO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author xd11cc
 * @date 2026-11-03
 * @description 系统文件元数据；对象存储里的文件必须先在库里有一条记录才允许访问
 */
@Data
@TableName("system_file")
@ApiModel(value = "SystemFileDO", description = "系统文件")
public class SystemFileDO extends BaseTenantDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键id", required = true)
    private Long id;

    /**
     * 对外暴露的文件标识，UUID；不含原始文件名，避免泄露与猜测
     */
    @ApiModelProperty(value = "文件唯一标识", required = true)
    private String fileId;

    /**
     * 对象存储中的 key，形如 {tenantId}/{yyyyMM}/{uuid}.{ext}
     */
    @ApiModelProperty(value = "对象存储key", required = true)
    private String objectKey;

    @ApiModelProperty(value = "对象存储桶名", required = true)
    private String bucketName;

    @ApiModelProperty(value = "原始文件名", required = true)
    private String originalName;

    @ApiModelProperty(value = "MIME 类型", required = true)
    private String contentType;

    @ApiModelProperty(value = "文件大小（字节）", required = true)
    private Long sizeBytes;

    /**
     * 字典 system_file_status：0-临时（未被业务引用，可清理）、1-已引用
     */
    @ApiModelProperty(value = "文件状态", required = true)
    private String status;

    @ApiModelProperty(value = "备注", required = false)
    private String remark;
}
