package com.ylum.cloudgallery.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 空间用量排行视图对象。
 *
 * <p>统计各空间的图片总大小，用于按总大小降序的用量排行。</p>
 */
@Data
@Schema(description = "空间用量排行")
public class SpaceRankAnalyzeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 空间 ID */
    @Schema(description = "空间 ID")
    private Long spaceId;

    /** 图片数量 */
    @Schema(description = "图片数量")
    private Long count;

    /** 图片总大小（字节） */
    @Schema(description = "图片总大小（字节）")
    private Long totalSize;
}
