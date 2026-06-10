package com.xd11cc.single.config.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author xd11cc
 * @date 2026-06-05 15:32:14
 * @description
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PayClientException extends RuntimeException {

    public PayClientException(Throwable cause) {
        super(cause);
    }
}
