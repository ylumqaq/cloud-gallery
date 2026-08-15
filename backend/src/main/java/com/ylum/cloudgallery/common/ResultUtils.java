package com.ylum.cloudgallery.common;

/**
 * 统一返回工具类，提供静态工厂方法，避免控制器内重复 {@code new BaseResponse(...)}。
 */
public class ResultUtils {

    /**
     * 成功（携带数据）
     *
     * @param data 业务数据
     * @param <T>  业务数据类型
     * @return 成功响应
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(ErrorCode.SUCCESS.getCode(), data, ErrorCode.SUCCESS.getMessage());
    }

    /**
     * 成功（无数据）
     *
     * @param <T> 业务数据类型
     * @return 成功响应
     */
    public static <T> BaseResponse<T> success() {
        return success(null);
    }

    /**
     * 失败：指定错误码与提示信息
     *
     * @param code    错误码
     * @param message 提示信息
     * @param <T>     业务数据类型
     * @return 失败响应
     */
    public static <T> BaseResponse<T> error(int code, String message) {
        return new BaseResponse<>(code, null, message);
    }

    /**
     * 失败：按错误码枚举构造（使用枚举默认提示信息）
     *
     * @param errorCode 错误码枚举
     * @param <T>       业务数据类型
     * @return 失败响应
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMessage());
    }

    /**
     * 失败：按错误码枚举构造，并覆盖提示信息
     *
     * @param errorCode 错误码枚举
     * @param message   自定义提示信息
     * @param <T>       业务数据类型
     * @return 失败响应
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode, String message) {
        return error(errorCode.getCode(), message);
    }
}
