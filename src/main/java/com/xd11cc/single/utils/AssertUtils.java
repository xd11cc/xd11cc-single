package com.xd11cc.single.utils;

import com.xd11cc.single.config.exception.ErrorCode;
import com.xd11cc.single.config.exception.ServiceException;
import org.springframework.util.CollectionUtils;

import java.util.Collection;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
public class AssertUtils {

    public static void notNull(Object obj, ErrorCode errorCode) {
        if (obj == null) {
            throw new ServiceException(errorCode);
        }
    }

    public static void notEmpty(String str, ErrorCode errorCode) {
        if (str == null || str.trim().isEmpty()) {
            throw new ServiceException(errorCode);
        }
    }

    public static void notEmpty(Collection<?> coll, ErrorCode errorCode) {
        if (CollectionUtils.isEmpty(coll)) {
            throw new ServiceException(errorCode);
        }
    }

    public static void isTrue(boolean condition, ErrorCode errorCode) {
        if (!condition) {
            throw new ServiceException(errorCode);
        }
    }

    public static void isFalse(boolean condition, ErrorCode errorCode) {
        if (condition) {
            throw new ServiceException(errorCode);
        }
    }
}
