package com.xd11cc.single.exception;


import lombok.Data;
import lombok.EqualsAndHashCode;
import org.slf4j.helpers.MessageFormatter;

/**
 * @Author: xd11cc
 * @Date: 2025/6/18 15:26
 *
 * 业务异常
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private ErrorCode errorCode;

    private Object[] args;

    public ServiceException(ErrorCode errorCode) {
        super(errorCode.getErrorCode() + ":" + errorCode.getErrorMsg());
        this.errorCode = errorCode;
    }

    public ServiceException(ErrorCode errorCode, Object[] args) {
        super(errorCode.getErrorCode() + ":" + MessageFormatter.arrayFormat(errorCode.getErrorMsg(), args).getMessage());
        this.errorCode = errorCode;
        this.args = args;
    }

    public ServiceException(int errorCode, String errorMsg) {
        this(new ErrorCode() {
            @Override
            public int getErrorCode() {
                return errorCode;
            }

            @Override
            public String getErrorMsg() {
                return errorMsg;
            }
        });
    }

    public ServiceException(int errorCode, String errorMsg, Object[] args) {
        this(new ErrorCode() {
            @Override
            public int getErrorCode() {
                return errorCode;
            }

            @Override
            public String getErrorMsg() {
                return errorMsg;
            }
        }, args);
    }

}
