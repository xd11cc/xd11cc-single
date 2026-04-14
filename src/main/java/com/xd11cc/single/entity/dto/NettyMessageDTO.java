package com.xd11cc.single.entity.dto;

import com.xd11cc.single.enums.WebSocketEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xd11cc
 * @date 2026-03-27 17:42:04
 * @description
 */
@Data
public class NettyMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息id
     */
    private Long id;

    /**
     * 消息类型
     */
    private WebSocketEnum webSocketEnum;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 租户id
     */
    private Long tenantId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 发送时间
     */
    private Date sendTime;
}
