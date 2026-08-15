package com.ylum.cloudgallery.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 空间使用分析视图对象。
 *
 * <p>统计指定空间（或公共图库）已用图片数量与总大小，并结合全局上限计算使用率。</p>
 */
@Data
@Schema(description = "空间使用分析")
public class SpaceUsageAnalyzeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 已用图片数量 */
    @Schema(description = "已用图片数量")
    private Long usedCount;

    /** 已用总大小（字节） */
    @Schema(description = "已用总大小（字节）")
    private Long usedSize;

    /** 图片数量上限 */
    @Schema(description = "图片数量上限")
    private Long maxCount;

    /** 总大小上限（字节） */
    @Schema(description = "总大小上限（字节）")
    private Long maxSize;

    /** 数量使用率（百分比，0-100） */
    @Schema(description = "数量使用率（百分比，0-100）")
    private Double countUsageRatio;

    /** 大小使用率（百分比，0-100） */
    @Schema(description = "大小使用率（百分比，0-100）")
    private Double sizeUsageRatio;
}
