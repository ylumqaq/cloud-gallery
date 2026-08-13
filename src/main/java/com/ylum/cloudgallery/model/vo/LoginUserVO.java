package com.ylum.cloudgallery.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 登录成功返回对象，在用户脱敏信息基础上携带登录凭证 token。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "登录成功返回（脱敏信息 + token）")
public class LoginUserVO extends UserVO {

    private static final long serialVersionUID = 1L;

    /** 登录凭证 token（由 Sa-Token 生成，登录态存 Redis） */
    @Schema(description = "登录凭证 token")
    private String token;
}
