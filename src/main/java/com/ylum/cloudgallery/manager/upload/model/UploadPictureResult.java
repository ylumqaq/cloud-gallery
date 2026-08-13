package com.ylum.cloudgallery.manager.upload.model;

import lombok.Data;

/**
 * 图片上传结果，承载上传完成后写入 {@code picture} 表所需的元数据。
 */
@Data
public class UploadPictureResult {

    /** 原图 URL（COS） */
    private String url;

    /** 缩略图 URL（COS） */
    private String thumbnailUrl;

    /** 图片名称 */
    private String picName;

    /** 图片大小（字节） */
    private Long picSize;

    /** 图片宽度 */
    private Integer picWidth;

    /** 图片高度 */
    private Integer picHeight;

    /** 图片格式 */
    private String picFormat;

    /** 平均主色调 */
    private String picColor;

    /** 原图对象键（COS，用于上传失败补偿删除） */
    private String originalKey;

    /** webp 压缩图对象键（COS，用于上传失败补偿删除） */
    private String webpKey;

    /** 缩略图对象键（COS，用于上传失败补偿删除） */
    private String thumbnailKey;
}
