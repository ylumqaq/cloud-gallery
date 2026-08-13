package com.ylum.cloudgallery.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 编辑图片请求参数（改名 + 移动空间）。
 */
@Data
@Schema(description = "编辑图片请求")
public class PictureEditRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 图片 ID */
    @Schema(description = "图片 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "图片 ID 不能为空")
    private Long id;

    /** 新图片名称 */
    @Schema(description = "新图片名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "图片名称不能为空")
    @Size(max = 128, message = "图片名称不能超过 128 个字符")
    private String name;

    /** 图片分类（可选，用于分类分析） */
    @Schema(description = "图片分类（可选，用于分类分析）")
    @Size(max = 128, message = "图片分类不能超过 128 个字符")
    private String category;

    /** 图片标签（可选，JSON 数组字符串，用于标签分析） */
    @Schema(description = "图片标签（可选，JSON 数组字符串，用于标签分析）")
    @Size(max = 512, message = "图片标签不能超过 512 个字符")
    private String tags;

    /** 目标空间 ID（空 = 公共图库） */
    @Schema(description = "目标空间 ID（空 = 公共图库）")
    private Long spaceId;
}
