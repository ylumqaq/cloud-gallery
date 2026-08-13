package com.ylum.cloudgallery.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询图片请求参数。
 */
@Data
@Schema(description = "分页查询图片请求")
public class PictureQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页 */
    @Schema(description = "当前页")
    private int current = 1;

    /** 每页大小 */
    @Schema(description = "每页大小")
    private int pageSize = 10;

    /** 空间 ID（空 = 公共图库） */
    @Schema(description = "空间 ID（空 = 公共图库）")
    private Long spaceId;

    /** 名称关键词 */
    @Schema(description = "名称关键词")
    private String searchText;

    /** 主色调（按颜色搜索） */
    @Schema(description = "主色调")
    private String picColor;
}
