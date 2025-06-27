package com.xd11cc.single.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @Author: xd11cc
 * @Date: 2025/6/27 23:09
 *
 * 自定义限流异常
 **/
@ResponseStatus(code = HttpStatus.TOO_MANY_REQUESTS, reason = "Rate limit exceeded")
public class RateLimitException extends RuntimeException {

    public RateLimitException(String message) {
        super(message);
    }
}
