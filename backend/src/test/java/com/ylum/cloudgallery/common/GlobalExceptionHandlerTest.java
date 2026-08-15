package com.ylum.cloudgallery.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 全局异常处理器单元测试：验证业务异常与兜底异常被正确转换为 {@link BaseResponse}。
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    /**
     * 业务异常处理：应返回异常携带的错误码与提示信息，data 为 null。
     */
    @Test
    void businessExceptionHandler_shouldReturnBusinessCodeAndMessage() {
        BaseResponse<?> response = handler.businessExceptionHandler(
                new BusinessException(ErrorCode.NOT_FOUND_ERROR));

        assertEquals(ErrorCode.NOT_FOUND_ERROR.getCode(), response.getCode());
        assertEquals(ErrorCode.NOT_FOUND_ERROR.getMessage(), response.getMessage());
        assertNull(response.getData());
    }

    /**
     * 兜底异常处理：应返回系统内部异常 50000。
     */
    @Test
    void exceptionHandler_shouldReturnSystemError() {
        BaseResponse<?> response = handler.exceptionHandler(new RuntimeException("boom"));

        assertEquals(ErrorCode.SYSTEM_ERROR.getCode(), response.getCode());
        assertEquals(ErrorCode.SYSTEM_ERROR.getMessage(), response.getMessage());
        assertNull(response.getData());
    }
}
