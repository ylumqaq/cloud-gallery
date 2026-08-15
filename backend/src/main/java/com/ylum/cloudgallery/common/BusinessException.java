package com.ylum.cloudgallery.common;

import lombok.Getter;

/**
 * 业务异常，抛出时携带错误码与提示信息，由全局异常处理器统一捕获。
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 错误码 */
    private final int code;

    /**
     * 指定错误码与提示信息构造
     *
     * @param code    错误码
     * @param message 提示信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 按错误码枚举构造（使用枚举默认提示信息）
     *
     * @param errorCode 错误码枚举
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    /**
     * 按错误码枚举构造，并覆盖提示信息
     *
     * @param errorCode 错误码枚举
     * @param message   自定义提示信息
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}
