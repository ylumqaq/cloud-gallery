package com.ylum.cloudgallery.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户脱敏视图对象，不包含密码等敏感字段。
 */
@Data
@Schema(description = "用户脱敏信息（不含密码）")
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户 ID */
    @Schema(description = "用户 ID")
    private Long id;

    /** 登录账号 */
    @Schema(description = "登录账号")
    private String userAccount;

    /** 昵称 */
    @Schema(description = "昵称")
    private String userName;

    /** 头像 URL */
    @Schema(description = "头像 URL")
    private String userAvatar;

    /** 个人简介 */
    @Schema(description = "个人简介")
    private String userProfile;

    /** 用户角色（user / admin / super_admin） */
    @Schema(description = "用户角色（user / admin / super_admin）")
    private String userRole;

    /** 创建时间 */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
