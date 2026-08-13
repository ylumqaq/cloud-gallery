package com.ylum.cloudgallery.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 空间大小分析视图对象。
 *
 * <p>按图片 {@code pic_size} 落点区间分组，统计每个大小区间内的图片数量。</p>
 */
@Data
@Schema(description = "空间大小分析")
public class SpaceSizeAnalyzeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 大小区间，例如 &lt;100KB / 100KB-500KB / 500KB-1MB / &gt;1MB */
    @Schema(description = "大小区间")
    private String sizeRange;

    /** 该区间内图片数量 */
    @Schema(description = "图片数量")
    private Long count;
}
