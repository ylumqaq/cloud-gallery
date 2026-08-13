package com.ylum.cloudgallery.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 编辑空间请求参数。
 */
@Data
@Schema(description = "编辑空间请求")
public class SpaceEditRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 空间 ID */
    @Schema(description = "空间 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "空间 ID 不能为空")
    private Long id;

    /** 新空间名称 */
    @Schema(description = "新空间名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "空间名称不能为空")
    @Size(max = 128, message = "空间名称不能超过 128 个字符")
    private String spaceName;
}
