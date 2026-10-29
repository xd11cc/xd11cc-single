package com.xd11cc.single.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * WebSocket 推送信封。
 * <p>
 * 信封负责「谁发的、什么动作」，业务载荷放在 {@link #data} 里原样透传——
 * 服务端不需要理解载荷结构。
 * </p>
 *
 * @author xd11cc
 * @date 2026-05-29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WsPushMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 动作标识，客户端据此分发（BUSINESS / NEW_NOTICE / WORKFLOW_* / ERROR）
     */
    private String action;

    /**
     * 发送者。仅客户端转发的消息有值，服务端事件推送为 null。
     * <p>
     * 由服务端从连接属性写入，<b>不采信客户端报文里的自述身份</b>。
     * </p>
     */
    private Sender from;

    /**
     * 业务载荷，原样透传
     */
    private Object data;

    public static WsPushMessage of(String action, Object data) {
        return new WsPushMessage(action, null, data);
    }

    /**
     * 发送者信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Sender implements Serializable {

        private static final long serialVersionUID = 1L;

        private Long userId;

        private String username;
    }
}
