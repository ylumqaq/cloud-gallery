package com.ylum.cloudgallery.mapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PictureEmbeddingMapper 集成测试：验证 pgvector 的写入 / 删除 / 余弦检索 SQL。
 *
 * <p>使用真实 PostgreSQL（pgvector），采用固定的测试 picture_id 并在用例结束后清理，
 * 避免污染业务数据。注意：PG 与 MySQL 为独立数据源，本测试方法不做 Spring 事务回滚。</p>
 */
@SpringBootTest
class PictureEmbeddingMapperTest {

    /** 测试用图片 ID（与真实业务 ID 区分，避免冲突） */
    private static final long TEST_PICTURE_ID_1 = 900000001L;
    private static final long TEST_PICTURE_ID_2 = 900000002L;
    private static final long TEST_SPACE_ID = 900000100L;

    @Resource
    private PictureEmbeddingMapper pictureEmbeddingMapper;

    @AfterEach
    void cleanup() {
        pictureEmbeddingMapper.deleteByPictureId(TEST_PICTURE_ID_1);
        pictureEmbeddingMapper.deleteByPictureId(TEST_PICTURE_ID_2);
    }

    /**
     * 生成 512 维全等值测试向量。
     */
    private List<Float> vector(float value) {
        return Collections.nCopies(512, value);
    }

    /**
     * 写入后应能按相似度检索到该图片 ID。
     */
    @Test
    void upsert_thenSearch_returnsPictureId() {
        List<Float> embedding = vector(1.0f);
        pictureEmbeddingMapper.upsert(TEST_PICTURE_ID_1, null, embedding);

        List<Long> ids = pictureEmbeddingMapper.searchSimilarPictureIds(embedding, null, 10);

        assertTrue(ids.contains(TEST_PICTURE_ID_1));
    }

    /**
     * 删除后不应再检索到该图片 ID（双删）。
     */
    @Test
    void delete_removesVector() {
        List<Float> embedding = vector(1.0f);
        pictureEmbeddingMapper.upsert(TEST_PICTURE_ID_1, null, embedding);
        pictureEmbeddingMapper.deleteByPictureId(TEST_PICTURE_ID_1);

        List<Long> ids = pictureEmbeddingMapper.searchSimilarPictureIds(embedding, null, 10);

        assertFalse(ids.contains(TEST_PICTURE_ID_1));
    }

    /**
     * 按 spaceId 过滤：公共图库（space_id 为空）与指定空间应互不可见。
     */
    @Test
    void search_filtersBySpaceId() {
        List<Float> embedding = vector(1.0f);
        // 一张落公共图库（space_id 为空），一张落指定空间
        pictureEmbeddingMapper.upsert(TEST_PICTURE_ID_1, null, embedding);
        pictureEmbeddingMapper.upsert(TEST_PICTURE_ID_2, TEST_SPACE_ID, embedding);

        // 搜公共图库：只应命中 space_id 为空的图片
        List<Long> publicIds = pictureEmbeddingMapper.searchSimilarPictureIds(embedding, null, 10);
        assertTrue(publicIds.contains(TEST_PICTURE_ID_1));
        assertFalse(publicIds.contains(TEST_PICTURE_ID_2));

        // 搜指定空间：只应命中该空间的图片
        List<Long> spaceIds = pictureEmbeddingMapper.searchSimilarPictureIds(embedding, TEST_SPACE_ID, 10);
        assertTrue(spaceIds.contains(TEST_PICTURE_ID_2));
        assertFalse(spaceIds.contains(TEST_PICTURE_ID_1));
    }
}
