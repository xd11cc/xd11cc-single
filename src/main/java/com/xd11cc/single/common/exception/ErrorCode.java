package com.xd11cc.single.common.exception;

import java.io.Serializable;

/**
 * @Author: xd11cc
 * @Date: 2025/6/18 16:43
 **/
public interface ErrorCode extends Serializable {

    int getErrorCode();

    String getErrorMsg();
}
