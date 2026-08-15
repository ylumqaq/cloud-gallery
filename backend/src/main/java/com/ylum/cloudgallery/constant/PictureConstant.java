package com.ylum.cloudgallery.constant;

import java.util.Set;

/**
 * 图片相关常量。
 */
public interface PictureConstant {

    /** 单张图片最大上传大小：2MB */
    long MAX_UPLOAD_SIZE = 2 * 1024 * 1024L;

    /** 允许上传的图片格式 */
    Set<String> ALLOWED_FORMATS = Set.of("jpg", "jpeg", "png", "webp");

    /** 缩略图处理尺寸（腾讯云 CI imageMogr2/thumbnail 参数） */
    String THUMBNAIL_SIZE = "200x200";

    /** 批量抓取默认数量 */
    int DEFAULT_BATCH_COUNT = 10;

    /** 以图搜图默认返回数量 */
    int DEFAULT_SEARCH_TOP_K = 20;
}
