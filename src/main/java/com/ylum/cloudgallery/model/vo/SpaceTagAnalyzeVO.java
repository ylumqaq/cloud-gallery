package com.ylum.cloudgallery.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 空间标签分析视图对象。
 *
 * <p>图片 {@code tags} 字段以 JSON 数组字符串存储，统计每个标签出现的次数与对应图片总大小。</p>
 */
@Data
@Schema(description = "空间标签分析")
public class SpaceTagAnalyzeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 标签名 */
    @Schema(description = "标签名")
    private String tag;

    /** 使用该标签的图片数量 */
    @Schema(description = "图片数量")
    private Long count;

    /** 使用该标签的图片总大小（字节） */
    @Schema(description = "图片总大小（字节）")
    private Long totalSize;
}
