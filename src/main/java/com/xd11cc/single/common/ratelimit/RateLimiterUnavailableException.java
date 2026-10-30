package com.xd11cc.single.common.ratelimit;

/**
 * 限流组件不可用（如 Redis 故障）。
 * <p>
 * 机制层不替调用方决定"降级放行还是降级拒绝"——那是策略。
 * HTTP 入口选择 fail-closed（拒绝），WebSocket 入口选择 fail-open（放行），
 * 由各自的调用方捕获本异常后决定。
 * </p>
 *
 * @author xd11cc
 */
public class RateLimiterUnavailableException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RateLimiterUnavailableException(String bucketKey, Throwable cause) {
        super("限流组件不可用: " + bucketKey, cause);
    }
}
