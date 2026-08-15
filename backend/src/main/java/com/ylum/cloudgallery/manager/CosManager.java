package com.ylum.cloudgallery.manager;

import cn.hutool.core.util.StrUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.ciModel.common.ImageProcessRequest;
import com.qcloud.cos.model.ciModel.image.ImageInfos;
import com.qcloud.cos.model.ciModel.image.ImageSearchRequest;
import com.qcloud.cos.model.ciModel.image.ImageSearchResponse;
import com.qcloud.cos.model.ciModel.image.OpenImageSearchRequest;
import com.qcloud.cos.model.ciModel.persistence.CIUploadResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.ylum.cloudgallery.config.CosClientConfig;
import com.ylum.cloudgallery.constant.PictureConstant;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 腾讯云 COS + 数据万象 CI 封装。
 *
 * <p>负责对象存储的「文件上传 / 删除 / 访问地址生成」，以及通过 CI 云上数据处理完成
 * webp 压缩、缩略图生成与图片信息（宽高 / 格式 / 主色调）提取。</p>
 */
@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传对象到 COS（纯上传，不做图片处理）。
     *
     * @param key  COS 对象键（存储路径，不含前导斜杠）
     * @param file 本地文件
     */
    public void putObject(String key, File file) {
        PutObjectRequest request = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        cosClient.putObject(request);
    }

    /**
     * 触发 CI 云上数据处理：webp 压缩 + 缩略图，并返回原图信息。
     *
     * @param key               原图对象键
     * @param webpFileName      webp 压缩结果文件名（相对原图所在目录）
     * @param thumbnailFileName 缩略图结果文件名（相对原图所在目录）
     * @return 原图信息（宽高 / 格式 / 主色调），CI 未返回时为 null
     */
    public ImageInfo processImage(String key, String webpFileName, String thumbnailFileName) {
        ImageProcessRequest request = new ImageProcessRequest(cosClientConfig.getBucket(), key);

        PicOperations picOperations = new PicOperations();
        // 1 表示返回原图信息（宽高 / 格式 / 主色调）
        picOperations.setIsPicInfo(1);

        List<PicOperations.Rule> rules = new ArrayList<>();

        // 规则一：转 webp 压缩，减小存储体积
        PicOperations.Rule webpRule = new PicOperations.Rule();
        webpRule.setBucket(cosClientConfig.getBucket());
        webpRule.setFileId(webpFileName);
        webpRule.setRule("imageMogr2/format/webp");
        rules.add(webpRule);

        // 规则二：生成缩略图
        PicOperations.Rule thumbnailRule = new PicOperations.Rule();
        thumbnailRule.setBucket(cosClientConfig.getBucket());
        thumbnailRule.setFileId(thumbnailFileName);
        thumbnailRule.setRule("imageMogr2/thumbnail/" + PictureConstant.THUMBNAIL_SIZE);
        rules.add(thumbnailRule);

        picOperations.setRules(rules);
        request.setPicOperations(picOperations);

        CIUploadResult result = cosClient.processImage(request);
        if (result == null || result.getOriginalInfo() == null) {
            return null;
        }
        return result.getOriginalInfo().getImageInfo();
    }

    /**
     * 获取对象访问 URL。
     *
     * @param key COS 对象键
     * @return 对象访问 URL
     */
    public String getObjectUrl(String key) {
        return cosClient.getObjectUrl(cosClientConfig.getBucket(), key).toString();
    }

    /**
     * 删除 COS 对象（幂等，对象不存在也不会抛错）。
     *
     * @param key COS 对象键
     */
    public void deleteObject(String key) {
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
    }

    /**
     * 下载 COS 对象到本地文件。
     *
     * @param key      COS 对象键
     * @param destFile 目标本地文件
     * @throws IOException 下载或写入失败
     */
    public void downloadObject(String key, File destFile) throws IOException {
        COSObject cosObject = cosClient.getObject(cosClientConfig.getBucket(), key);
        try (InputStream in = cosObject.getObjectContent();
             OutputStream out = new FileOutputStream(destFile)) {
            in.transferTo(out);
        }
    }

    /**
     * 开通 Bucket 以图搜图图库（一次性操作，图库容量设置后不可修改）。
     *
     * @param maxCapacity 图库容量限制（必填，如 "10000"）
     * @param maxQps      图库访问 QPS 限制（可选，默认 10）
     */
    public void openImageSearch(String maxCapacity, String maxQps) {
        OpenImageSearchRequest request = new OpenImageSearchRequest();
        request.setBucketName(cosClientConfig.getBucket());
        request.setMaxCapacity(maxCapacity);
        request.setMaxQps(maxQps);
        cosClient.openImageSearch(request);
    }

    /**
     * 将 COS 上的图片加入 CI 以图搜图图库（入库时 CI 自动提取图片特征）。
     *
     * @param key       COS 对象键
     * @param pictureId 图片 ID（作为 entityId 保存，检索时原样带回）
     */
    public void addGalleryImage(String key, long pictureId) {
        ImageSearchRequest request = new ImageSearchRequest();
        request.setBucketName(cosClientConfig.getBucket());
        request.setObjectKey(key);
        request.setEntityId(String.valueOf(pictureId));
        cosClient.addGalleryImages(request);
    }

    /**
     * 从 CI 以图搜图图库移除指定图片。
     *
     * @param key       COS 对象键
     * @param pictureId 图片 ID（作为 entityId，与入库时一致）
     */
    public void deleteGalleryImage(String key, long pictureId) {
        ImageSearchRequest request = new ImageSearchRequest();
        request.setBucketName(cosClientConfig.getBucket());
        request.setObjectKey(key);
        request.setEntityId(String.valueOf(pictureId));
        cosClient.deleteGalleryImages(request);
    }

    /**
     * 调用 CI 以图搜图接口，返回相似图片 ID 列表（按相似度降序）。
     *
     * @param queryKey 查询图对象键（需已上传到 COS）
     * @param limit    返回数量上限
     * @return 相似图片 ID 列表（对应入库时的 entityId）
     */
    public List<Long> searchGalleryImages(String queryKey, int limit) {
        ImageSearchRequest request = new ImageSearchRequest();
        request.setBucketName(cosClientConfig.getBucket());
        request.setObjectKey(queryKey);
        request.setLimit(String.valueOf(limit));

        ImageSearchResponse response = cosClient.searchGalleryImages(request);
        List<Long> pictureIds = new ArrayList<>();
        if (response == null || response.getImageInfos() == null) {
            return pictureIds;
        }
        for (ImageInfos info : response.getImageInfos()) {
            if (StrUtil.isNotBlank(info.getEntityId())) {
                pictureIds.add(Long.valueOf(info.getEntityId()));
            }
        }
        return pictureIds;
    }
}
