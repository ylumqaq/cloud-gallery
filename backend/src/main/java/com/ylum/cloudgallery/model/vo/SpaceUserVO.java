package com.ylum.cloudgallery.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 空间成员视图对象，除成员关系外附带用户展示信息。
 */
@Data
@Schema(description = "空间成员信息")
public class SpaceUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 成员关系 ID */
    @Schema(description = "成员关系 ID")
    private Long id;

    /** 空间 ID */
    @Schema(description = "空间 ID")
    private Long spaceId;

    /** 用户 ID */
    @Schema(description = "用户 ID")
    private Long userId;

    /** 角色：viewer / editor / admin */
    @Schema(description = "角色：viewer / editor / admin")
    private String spaceRole;

    /** 加入时间 */
    @Schema(description = "加入时间")
    private LocalDateTime createTime;

    /** 用户昵称 */
    @Schema(description = "用户昵称")
    private String userName;

    /** 用户账号 */
    @Schema(description = "用户账号")
    private String userAccount;

    /** 用户头像 URL */
    @Schema(description = "用户头像 URL")
    private String userAvatar;
}
