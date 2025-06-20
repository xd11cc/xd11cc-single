package com.xd11cc.single.entity.vo.base;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: xd11cc
 * @Date: 2025/6/13 21:03
 **/
@Data
public class ResponseVO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成功
     */
    private static final int SUCCESS = 200;

    /**
     * 失败
     */
    private static final int FAIL = 500;

    private T data;

    private int code;

    private String msg;

    public static <T> ResponseVO<T> success() {
        return resetResult(null, SUCCESS, null);
    }

    public static <T> ResponseVO<T> success(T data) {
        return resetResult(data, SUCCESS, null);
    }

    public static <T> ResponseVO<T> success(T data, String msg) {
        return resetResult(data, SUCCESS, msg);
    }

    public static <T> ResponseVO<T> fail() {
        return resetResult(null, FAIL, "操作失败");
    }

    public static <T> ResponseVO<T> fail(String msg) {
        return resetResult(null, FAIL, msg);
    }

    public static <T> ResponseVO<T> fail(int code, String msg) {
        return resetResult(null, code, msg);
    }

    private static <T> ResponseVO<T> resetResult(T data, int code, String msg) {
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setData(data);
        responseVO.setCode(code);
        responseVO.setMsg(msg);
        return responseVO;
    }

}
