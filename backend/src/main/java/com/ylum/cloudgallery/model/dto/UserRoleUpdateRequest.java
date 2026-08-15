package com.ylum.cloudgallery.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 修改用户角色请求参数（仅高级管理员可用）。
 */
@Data
@Schema(description = "修改用户角色请求")
public class UserRoleUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 目标用户 ID */
    @Schema(description = "目标用户 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "目标用户 ID 不能为空")
    private Long targetUserId;

    /** 目标角色：user / admin / super_admin */
    @Schema(description = "目标角色：user / admin / super_admin", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "角色不能为空")
    private String userRole;
}
