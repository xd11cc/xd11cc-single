package com.xd11cc.single.entity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 客户端上行报文。
 * <p>
 * 只有两个字段：发给谁、发什么。没有「消息类型」这一类判别字段——
 * 广播属于服务端能力，不通过客户端报文表达，因此协议层面就不存在越权入口。
 * </p>
 *
 * @author xd11cc
 * @date 2026-03-27 17:42:04
 */
@Data
public class NettyMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 目标用户 id（同租户）
     */
    private Long userId;

    /**
     * 业务载荷。
     * <p>
     * 直接承载结构化对象，服务端不解析其结构；发送者身份由服务端注入信封。
     * </p>
     */
    private Object data;
}
