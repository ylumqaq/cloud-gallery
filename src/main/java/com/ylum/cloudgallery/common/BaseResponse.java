package com.ylum.cloudgallery.common;

import lombok.Data;

/**
 * 统一返回对象，所有接口均返回 {@code BaseResponse<T>} 结构。
 *
 * @param <T> 业务数据类型
 */
@Data
public class BaseResponse<T> {

    /** 状态码：0 表示成功，非 0 表示失败 */
    private int code;

    /** 业务数据（可为 null） */
    private T data;

    /** 提示信息 */
    private String message;

    /**
     * 全参构造方法
     *
     * @param code    状态码
     * @param data    业务数据
     * @param message 提示信息
     */
    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }
}
