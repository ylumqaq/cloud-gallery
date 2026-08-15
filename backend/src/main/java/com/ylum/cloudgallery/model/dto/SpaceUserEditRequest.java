package com.ylum.cloudgallery.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 修改空间成员角色请求参数。
 */
@Data
@Schema(description = "修改空间成员角色请求")
public class SpaceUserEditRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 空间 ID */
    @Schema(description = "空间 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "空间 ID 不能为空")
    private Long spaceId;

    /** 成员用户 ID */
    @Schema(description = "成员用户 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户 ID 不能为空")
    private Long userId;

    /** 新角色：viewer / editor / admin */
    @Schema(description = "新角色：viewer / editor / admin", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "角色不能为空")
    private String spaceRole;
}
