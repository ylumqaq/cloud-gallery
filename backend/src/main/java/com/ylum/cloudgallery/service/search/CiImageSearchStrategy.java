package com.ylum.cloudgallery.service.search;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.ylum.cloudgallery.manager.CosManager;
import com.ylum.cloudgallery.manager.upload.model.UploadPictureResult;
import com.ylum.cloudgallery.model.entity.Picture;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * 腾讯云数据万象 CI 托管图片检索。
 *
 * <p>不提取中间向量，直接复用 CI 的「以图搜图」能力：上传成功后把图片对象加入 CI 图库
 * （{@code entityId} 存图片 ID），检索时把查询图临时上传到 COS 后调用 CI 检索接口，返回的
 * {@code entityId} 即图片 ID。删除时同步从 CI 图库出库。</p>
 */
@Slf4j
@Component
public class CiImageSearchStrategy {

    @Resource
    private CosManager cosManager;

    public void onUpload(Picture picture, UploadPictureResult result) {
        try {
            // entityId 保存图片 ID，检索时由 CI 原样带回
            cosManager.addGalleryImage(result.getOriginalKey(), picture.getId());
        } catch (Exception e) {
            log.warn("CI 图库入库失败（最终一致，不影响图片上传），pictureId={}", picture.getId(), e);
        }
    }

    public void onDelete(Picture picture) {
        try {
            String key = extractKey(picture.getUrl());
            if (key != null) {
                cosManager.deleteGalleryImage(key, picture.getId());
            }
        } catch (Exception e) {
            log.warn("CI 图库出库失败（最终一致），pictureId={}", picture.getId(), e);
        }
    }

    public List<Long> search(File queryFile, Long spaceId, int topK) {
        // CI 检索要求查询图位于 COS 上，先上传到临时对象，检索完成后删除
        String suffix = FileUtil.getSuffix(queryFile.getName());
        if (StrUtil.isBlank(suffix)) {
            suffix = "jpg";
        }
        String queryKey = "search/query/" + RandomUtil.randomString(16) + "." + suffix;
        cosManager.putObject(queryKey, queryFile);
        try {
            return cosManager.searchGalleryImages(queryKey, topK);
        } finally {
            try {
                cosManager.deleteObject(queryKey);
            } catch (Exception e) {
                log.warn("清理查询临时对象失败，queryKey={}", queryKey, e);
            }
        }
    }

    /**
     * 从对象 URL 提取对象键（URL 路径去掉前导斜杠）。
     */
    private String extractKey(String url) {
        if (StrUtil.isBlank(url)) {
            return null;
        }
        try {
            String path = new URL(url).getPath();
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
