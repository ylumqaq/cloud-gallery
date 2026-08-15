package com.ylum.cloudgallery.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 图片视图对象。
 */
@Data
@Schema(description = "图片信息")
public class PictureVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 图片 ID */
    @Schema(description = "图片 ID")
    private Long id;

    /** 图片名称 */
    @Schema(description = "图片名称")
    private String name;

    /** 原图 URL */
    @Schema(description = "原图 URL")
    private String url;

    /** 缩略图 URL */
    @Schema(description = "缩略图 URL")
    private String thumbnailUrl;

    /** 图片大小（字节） */
    @Schema(description = "图片大小（字节）")
    private Long picSize;

    /** 图片宽度 */
    @Schema(description = "图片宽度")
    private Integer picWidth;

    /** 图片高度 */
    @Schema(description = "图片高度")
    private Integer picHeight;

    /** 图片格式 */
    @Schema(description = "图片格式")
    private String picFormat;

    /** 平均主色调 */
    @Schema(description = "平均主色调")
    private String picColor;

    /** 图片分类 */
    @Schema(description = "图片分类")
    private String category;

    /** 图片标签（JSON 数组字符串） */
    @Schema(description = "图片标签（JSON 数组字符串）")
    private String tags;

    /** 所属空间 ID（空 = 公共图库） */
    @Schema(description = "所属空间 ID（空 = 公共图库）")
    private Long spaceId;

    /** 上传者用户 ID */
    @Schema(description = "上传者用户 ID")
    private Long userId;

    /** 创建时间 */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /** 最近编辑时间 */
    @Schema(description = "最近编辑时间")
    private LocalDateTime editTime;
}
