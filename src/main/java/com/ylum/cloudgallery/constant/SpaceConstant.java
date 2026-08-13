package com.ylum.cloudgallery.constant;

/**
 * 空间相关常量。
 */
public interface SpaceConstant {

    /** 私有空间类型 */
    int SPACE_TYPE_PRIVATE = 0;

    /** 团队空间类型 */
    int SPACE_TYPE_TEAM = 1;

    /** 单个空间允许的最大图片数量（张），后续调整只需修改此处 */
    long DEFAULT_MAX_PICTURE_COUNT = 400;

    /** 单个空间允许的最大图片总大小（字节），默认 500MB，后续调整只需修改此处 */
    long DEFAULT_MAX_PICTURE_SIZE = 500L * 1024 * 1024;
}
