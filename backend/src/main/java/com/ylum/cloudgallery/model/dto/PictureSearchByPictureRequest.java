package com.ylum.cloudgallery.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * 以图搜图请求参数（本地文件与 URL 二选一）。
 */
@Data
@Schema(description = "以图搜图请求")
public class PictureSearchByPictureRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 查询图片文件（与 fileUrl 二选一） */
    @Schema(description = "查询图片文件（与 fileUrl 二选一）")
    private MultipartFile file;

    /** 查询图片 URL（与 file 二选一） */
    @Schema(description = "查询图片 URL（与 file 二选一）")
    private String fileUrl;

    /** 限定搜索的空间 ID（空 = 公共图库） */
    @Schema(description = "限定搜索的空间 ID（空 = 公共图库）")
    private Long spaceId;

    /** 返回最相似数量，默认 20 */
    @Schema(description = "返回最相似数量，默认 20")
    private Integer topK;
}
