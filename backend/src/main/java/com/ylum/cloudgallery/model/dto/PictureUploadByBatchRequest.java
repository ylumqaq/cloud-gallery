package com.ylum.cloudgallery.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 批量抓取上传请求参数。
 */
@Data
@Schema(description = "批量抓取上传请求")
public class PictureUploadByBatchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 抓取关键词 */
    @Schema(description = "抓取关键词", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "抓取关键词不能为空")
    private String searchText;

    /** 抓取数量 */
    @Schema(description = "抓取数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "抓取数量不能为空")
    private Integer count;

    /** 目标空间 ID（空 = 公共图库） */
    @Schema(description = "目标空间 ID（空 = 公共图库）")
    private Long spaceId;
}
