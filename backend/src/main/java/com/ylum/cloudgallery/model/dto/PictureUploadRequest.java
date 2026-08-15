package com.ylum.cloudgallery.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * 上传图片请求参数（本地文件与 URL 二选一）。
 */
@Data
@Schema(description = "上传图片请求")
public class PictureUploadRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 本地文件（与 fileUrl 二选一） */
    @Schema(description = "本地文件（与 fileUrl 二选一）")
    private MultipartFile file;

    /** 网络图片 URL（与 file 二选一） */
    @Schema(description = "网络图片 URL（与 file 二选一）")
    private String fileUrl;

    /** 所属空间 ID（空 = 公共图库） */
    @Schema(description = "所属空间 ID（空 = 公共图库）")
    private Long spaceId;

    /** 图片名称（可选，不传则使用原始文件名） */
    @Schema(description = "图片名称（可选，不传则使用原始文件名）")
    private String picName;

    /** 图片分类（可选，用于分类分析） */
    @Schema(description = "图片分类（可选，用于分类分析）")
    private String category;

    /** 图片标签（可选，JSON 数组字符串，用于标签分析） */
    @Schema(description = "图片标签（可选，JSON 数组字符串，用于标签分析）")
    private String tags;
}
