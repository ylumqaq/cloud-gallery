package com.ylum.cloudgallery.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 空间分类分析视图对象。
 *
 * <p>按图片 {@code category} 字段分组，统计每个分类下的图片数量与总大小。</p>
 */
@Data
@Schema(description = "空间分类分析")
public class SpaceCategoryAnalyzeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 图片分类（未分类时为空字符串） */
    @Schema(description = "图片分类")
    private String category;

    /** 该分类下图片数量 */
    @Schema(description = "图片数量")
    private Long count;

    /** 该分类下图片总大小（字节） */
    @Schema(description = "图片总大小（字节）")
    private Long totalSize;
}
