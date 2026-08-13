package com.ylum.cloudgallery.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求参数。
 */
@Data
@Schema(description = "用户注册请求")
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 登录账号 */
    @Schema(description = "登录账号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "账号不能为空")
    @Size(min = 4, max = 256, message = "账号长度需在 4 到 256 之间")
    private String userAccount;

    /** 密码 */
    @Schema(description = "密码（长度 8-512）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 512, message = "密码长度需在 8 到 512 之间")
    private String userPassword;

    /** 确认密码 */
    @Schema(description = "确认密码（需与密码一致）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "确认密码不能为空")
    private String checkPassword;
}
