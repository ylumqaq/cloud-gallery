package com.ylum.cloudgallery.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 添加空间成员请求参数。
 */
@Data
@Schema(description = "添加空间成员请求")
public class SpaceUserAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 空间 ID */
    @Schema(description = "空间 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "空间 ID 不能为空")
    private Long spaceId;

    /** 被添加的用户 ID */
    @Schema(description = "被添加的用户 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户 ID 不能为空")
    private Long userId;

    /** 角色：viewer / editor / admin */
    @Schema(description = "角色：viewer / editor / admin", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "角色不能为空")
    private String spaceRole;
}
