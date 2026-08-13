package com.ylum.cloudgallery.common;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器：统一将异常转换为 {@link BaseResponse}。
 *
 * <p>本阶段处理业务异常、参数校验异常与兜底异常；
 * Sa-Token 的登录 / 权限异常将在引入 Sa-Token 后补充对应处理器。</p>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 业务异常处理：返回业务错误码与提示信息
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException: {}", e.getMessage());
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常处理：取第一条校验错误信息返回，错误码为 40000
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<?> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse(ErrorCode.PARAMS_ERROR.getMessage());
        return ResultUtils.error(ErrorCode.PARAMS_ERROR.getCode(), message);
    }

    /**
     * 未登录异常处理（Sa-Token）：返回 40100
     */
    @ExceptionHandler(NotLoginException.class)
    public BaseResponse<?> notLoginExceptionHandler(NotLoginException e) {
        log.warn("未登录：{}", e.getMessage());
        return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR);
    }

    /**
     * 无权限 / 无角色异常处理（Sa-Token）：返回 40101
     */
    @ExceptionHandler({NotPermissionException.class, NotRoleException.class})
    public BaseResponse<?> noAuthExceptionHandler(Exception e) {
        log.warn("无权限：{}", e.getMessage());
        return ResultUtils.error(ErrorCode.NO_AUTH_ERROR);
    }

    /**
     * 兜底异常处理：返回系统内部异常 50000
     */
    @ExceptionHandler(Exception.class)
    public BaseResponse<?> exceptionHandler(Exception e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR);
    }
}
