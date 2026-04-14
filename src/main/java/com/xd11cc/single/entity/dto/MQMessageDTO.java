package com.xd11cc.single.entity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xd11cc
 * @date 2025-11-27 22:46:10
 */
@Data
public class MQMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息id
     */
    private Long id;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 发送时间
     */
    private Date sendTime;
}
