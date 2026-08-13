package com.ylum.cloudgallery.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.ylum.cloudgallery.common.BaseResponse;
import com.ylum.cloudgallery.common.ResultUtils;
import com.ylum.cloudgallery.model.dto.UserLoginRequest;
import com.ylum.cloudgallery.model.dto.UserRegisterRequest;
import com.ylum.cloudgallery.model.vo.LoginUserVO;
import com.ylum.cloudgallery.model.vo.UserVO;
import com.ylum.cloudgallery.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器：注册 / 登录 / 获取当前用户 / 退出。
 *
 * <p>接口前缀为 {@code /api/user}（上下文路径 /api 由 application.yml 配置）。</p>
 */
@Tag(name = "用户模块", description = "注册 / 登录 / 获取当前用户 / 退出")
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 注册（无需登录）。
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@Valid @RequestBody UserRegisterRequest request) {
        long userId = userService.userRegister(request);
        return ResultUtils.success(userId);
    }

    /**
     * 登录（无需登录），返回脱敏信息 + token。
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@Valid @RequestBody UserLoginRequest request) {
        return ResultUtils.success(userService.userLogin(request));
    }

    /**
     * 获取当前登录用户（需登录）。
     */
    @Operation(summary = "获取当前登录用户")
    @GetMapping("/get/login")
    @SaCheckLogin
    public BaseResponse<UserVO> getLoginUser() {
        return ResultUtils.success(userService.getLoginUser());
    }

    /**
     * 退出登录（需登录）。
     */
    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    @SaCheckLogin
    public BaseResponse<Boolean> userLogout() {
        userService.userLogout();
        return ResultUtils.success(true);
    }
}
