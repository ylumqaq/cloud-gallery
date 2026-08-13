package com.ylum.cloudgallery.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建空间请求参数。
 */
@Data
@Schema(description = "创建空间请求")
public class SpaceAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 空间名称 */
    @Schema(description = "空间名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "空间名称不能为空")
    @Size(max = 128, message = "空间名称不能超过 128 个字符")
    private String spaceName;

    /** 空间类型：0 私有 / 1 团队 */
    @Schema(description = "空间类型：0 私有 / 1 团队", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "空间类型不能为空")
    private Integer spaceType;
}
