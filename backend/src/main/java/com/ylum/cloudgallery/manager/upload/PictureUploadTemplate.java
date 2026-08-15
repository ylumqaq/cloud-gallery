package com.ylum.cloudgallery.manager.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.ylum.cloudgallery.common.BusinessException;
import com.ylum.cloudgallery.common.ErrorCode;
import com.ylum.cloudgallery.constant.PictureConstant;
import com.ylum.cloudgallery.manager.CosManager;
import com.ylum.cloudgallery.manager.upload.model.UploadPictureResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Date;

/**
 * 图片上传模板（模板方法模式）。
 *
 * <p>统一上传骨架：校验 → 生成存储路径 → 获取本地文件 → 上传 COS → CI 图片处理 →
 * 封装结果 → 清理临时文件。本地文件上传与 URL 上传仅「校验 / 获取文件名 / 处理文件来源」
 * 三个步骤不同，由子类实现。</p>
 */
@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    protected CosManager cosManager;

    /**
     * 模板方法：执行完整上传流程。
     *
     * @param inputSource      输入源（本地文件为 {@code MultipartFile}，URL 上传为 {@code String}）
     * @param uploadPathPrefix 上传路径前缀（公共图库为 public，空间为 space/{spaceId}）
     * @return 上传结果
     */
    public UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        // 1. 校验输入源（大小 / 格式 / URL 合法性）
        validPicture(inputSource);

        // 2. 生成对象键（日期目录 + 随机名 + 后缀）
        String originalFilename = getOriginalFilename(inputSource);
        String suffix = FileUtil.getSuffix(originalFilename);
        validSuffix(suffix);

        String datePath = DateUtil.format(new Date(), "yyyyMMdd");
        String uuid = RandomUtil.randomString(16);
        String originalKey = String.format("%s/%s/%s.%s", uploadPathPrefix, datePath, uuid, suffix);
        // CI 的 fileid 使用原图同目录下的简单文件名
        String webpFilename = String.format("%s_webp.webp", uuid);
        String thumbnailFilename = String.format("%s_thumbnail.%s", uuid, suffix);
        String webpKey = String.format("%s/%s/%s", uploadPathPrefix, datePath, webpFilename);
        String thumbnailKey = String.format("%s/%s/%s", uploadPathPrefix, datePath, thumbnailFilename);

        File tempFile = null;
        try {
            // 3. 创建临时文件并写入本地
            tempFile = File.createTempFile("upload_" + uuid, "." + suffix);
            processFile(inputSource, tempFile);

            // 4. 上传原图到 COS
            cosManager.putObject(originalKey, tempFile);

            // 5. CI 云上数据处理（webp 压缩 + 缩略图 + 图片信息）
            ImageInfo imageInfo = cosManager.processImage(originalKey, webpFilename, thumbnailFilename);

            // 6. 获取原图访问 URL
            String originalUrl = cosManager.getObjectUrl(originalKey);

            // 7. 封装结果
            return buildResult(originalFilename, originalUrl, originalKey, webpKey, thumbnailKey, tempFile.length(), imageInfo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("图片上传失败，inputSource={}", inputSource, e);
            // 上传失败时补偿删除原图对象，避免产生孤儿文件（删除幂等）
            try {
                cosManager.deleteObject(originalKey);
            } catch (Exception ex) {
                log.warn("补偿删除原图对象失败，key={}", originalKey, ex);
            }
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片上传失败");
        } finally {
            // 6. 清理临时文件
            deleteTempFile(tempFile);
        }
    }

    /**
     * 校验输入源。
     *
     * @param inputSource 输入源
     */
    protected abstract void validPicture(Object inputSource);

    /**
     * 获取原始文件名（用于命名与提取后缀）。
     *
     * @param inputSource 输入源
     * @return 原始文件名
     */
    protected abstract String getOriginalFilename(Object inputSource);

    /**
     * 将输入源写入本地临时文件（本地转存 或 URL 下载）。
     *
     * @param inputSource 输入源
     * @param file        目标临时文件
     * @throws Exception 处理异常
     */
    protected abstract void processFile(Object inputSource, File file) throws Exception;

    /**
     * 校验文件后缀是否在允许的格式范围内。
     */
    protected void validSuffix(String suffix) {
        if (StrUtil.isBlank(suffix) || !PictureConstant.ALLOWED_FORMATS.contains(suffix.toLowerCase())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "仅支持 jpg / jpeg / png / webp 格式");
        }
    }

    /**
     * 校验文件大小不超过上限。
     */
    protected void validSize(long size) {
        if (size > PictureConstant.MAX_UPLOAD_SIZE) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 2MB");
        }
    }

    /**
     * 根据上传产物封装结果。
     */
    private UploadPictureResult buildResult(String picName, String originalUrl, String originalKey,
                                            String webpKey, String thumbnailKey, long picSize, ImageInfo imageInfo) {
        UploadPictureResult result = new UploadPictureResult();
        result.setPicName(picName);
        result.setUrl(originalUrl);
        result.setThumbnailUrl(cosManager.getObjectUrl(thumbnailKey));
        result.setPicSize(picSize);
        result.setOriginalKey(originalKey);
        result.setWebpKey(webpKey);
        result.setThumbnailKey(thumbnailKey);
        if (imageInfo != null) {
            result.setPicWidth(imageInfo.getWidth());
            result.setPicHeight(imageInfo.getHeight());
            result.setPicFormat(imageInfo.getFormat());
            result.setPicColor(imageInfo.getAve());
        }
        return result;
    }

    /**
     * 删除临时文件。
     */
    private void deleteTempFile(File file) {
        if (file != null && file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                file.deleteOnExit();
            }
        }
    }
}
