package com.ylum.cloudgallery.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 删除图片请求参数。
 */
@Data
@Schema(description = "删除图片请求")
public class PictureDeleteRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 图片 ID */
    @Schema(description = "图片 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "图片 ID 不能为空")
    private Long id;
}
