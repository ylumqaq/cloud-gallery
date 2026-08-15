package com.ylum.cloudgallery.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 删除空间请求参数。
 */
@Data
@Schema(description = "删除空间请求")
public class SpaceDeleteRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 空间 ID */
    @Schema(description = "空间 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "空间 ID 不能为空")
    private Long id;
}
