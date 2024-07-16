package com.yannqing.mackradio.exception;

import com.yannqing.mackradio.common.Code;

/**
 * 自定义异常类
 *
 */
public class BusinessException extends RuntimeException {

    private final int code;

    private final String description;

    public BusinessException(int code, String description) {
        super(description);
        this.code = code;
        this.description = description;
    }

    public BusinessException(String description) {
        super(description);
        this.code = Code.FAILURE;
        this.description = description;
    }

    public BusinessException() {
        super();
        this.code = Code.FAILURE;
        this.description = "系统异常，请重试";
    }

    public BusinessException(Exception e) {
        super(e.getMessage());
        this.code = Code.FAILURE;
        this.description = e.getMessage();
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
