package com.ylum.cloudgallery.service.search;

import com.ylum.cloudgallery.manager.CosManager;
import com.ylum.cloudgallery.manager.upload.model.UploadPictureResult;
import com.ylum.cloudgallery.mapper.PictureEmbeddingMapper;
import com.ylum.cloudgallery.model.entity.Picture;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * 本地 pgvector 以图搜图策略。
 *
 * <p>上传时从 COS 下载原图、用 {@link LocalImageEmbeddingExtractor} 提取 512 维向量写入
 * PostgreSQL 的 {@code picture_embedding} 表；检索时提取查询图向量，在 pgvector 中按余弦距离
 * 召回最相似的图片 ID。删除时同步删除向量（双删）。</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "image-search", name = "strategy", havingValue = "pg", matchIfMissing = true)
public class PgVectorImageSearchStrategy implements ImageSearchStrategy {

    @Resource
    private CosManager cosManager;

    @Resource
    private LocalImageEmbeddingExtractor embeddingExtractor;

    @Resource
    private PictureEmbeddingMapper pictureEmbeddingMapper;

    @Override
    public String name() {
        return "pg";
    }

    @Override
    public void onUpload(Picture picture, UploadPictureResult result) {
        // 向量写入失败仅影响搜图召回，不影响图片上传主流程（最终一致）
        File tempFile = null;
        try {
            tempFile = File.createTempFile("embedding_" + picture.getId(), ".tmp");
            cosManager.downloadObject(result.getOriginalKey(), tempFile);
            List<Float> embedding = embeddingExtractor.extract(tempFile);
            pictureEmbeddingMapper.upsert(picture.getId(), picture.getSpaceId(), embedding);
        } catch (Exception e) {
            log.warn("写入图片向量失败（最终一致，不影响图片上传），pictureId={}", picture.getId(), e);
        } finally {
            deleteTempFile(tempFile);
        }
    }

    @Override
    public void onDelete(Picture picture) {
        try {
            pictureEmbeddingMapper.deleteByPictureId(picture.getId());
        } catch (Exception e) {
            log.warn("删除图片向量失败（最终一致），pictureId={}", picture.getId(), e);
        }
    }

    @Override
    public List<Long> search(File queryFile, Long spaceId, int topK) {
        List<Float> embedding = embeddingExtractor.extract(queryFile);
        return pictureEmbeddingMapper.searchSimilarPictureIds(embedding, spaceId, topK);
    }

    /**
     * 删除临时文件（忽略失败）。
     */
    private void deleteTempFile(File file) {
        if (file != null && file.exists() && !file.delete()) {
            file.deleteOnExit();
        }
    }
}
