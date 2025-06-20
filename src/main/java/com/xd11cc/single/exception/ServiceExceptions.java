package com.xd11cc.single.exception;

/**
 * @Author: xd11cc
 * @Date: 2025/6/18 17:10
 **/
public final class ServiceExceptions {

    public static void throwWithErrorCode(ErrorCode errorCode) throws ServiceException {
        throw new ServiceException(errorCode);
    }

    public static void throwWithErrorCode(ErrorCode errorCode, Object[] args) throws ServiceException {
        throw new ServiceException(errorCode, args);
    }

    public static void throwWithCodeAndMessage(int errorCode, String errorMsg) throws ServiceException {
        throw new ServiceException(errorCode, errorMsg);
    }

    public static void throwWithCodeAndMessage(int errorCode, String errorMsg, Object[] args) throws ServiceException {
        throw new ServiceException(errorCode, errorMsg, args);
    }
}
