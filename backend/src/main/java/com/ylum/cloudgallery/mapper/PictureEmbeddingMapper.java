package com.ylum.cloudgallery.mapper;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PostgreSQL 向量表 {@code picture_embedding} 访问器（pgvector）。
 *
 * <p>通过独立 PG 数据源（{@code pgJdbcTemplate}）操作向量表，与 MySQL 业务库隔离。
 * 向量在 JDBC 层以 pgvector 的字符串字面量格式 {@code [x1,x2,...]} 传递，并借助
 * {@code CAST(? AS vector)} 完成类型转换；相似度检索使用 pgvector 的余弦距离运算符
 * {@code <=>}（值越小越相似）。</p>
 */
@Repository
public class PictureEmbeddingMapper {

    private final JdbcTemplate jdbcTemplate;

    public PictureEmbeddingMapper(@Qualifier("pgJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 写入（或更新）图片向量，幂等：已存在同 {@code picture_id} 时覆盖。
     *
     * @param pictureId 图片 ID（对应 MySQL picture.id）
     * @param spaceId   冗余空间 ID（公共图库为 null）
     * @param embedding 图片特征向量（512 维）
     */
    public void upsert(long pictureId, Long spaceId, List<Float> embedding) {
        String sql = "INSERT INTO picture_embedding (picture_id, space_id, embedding) "
                + "VALUES (?, ?, CAST(? AS vector)) "
                + "ON CONFLICT (picture_id) DO UPDATE SET space_id = EXCLUDED.space_id, embedding = EXCLUDED.embedding";
        jdbcTemplate.update(sql, pictureId, spaceId, toVectorString(embedding));
    }

    /**
     * 删除指定图片的向量（双删时调用）。
     *
     * @param pictureId 图片 ID
     */
    public void deleteByPictureId(long pictureId) {
        jdbcTemplate.update("DELETE FROM picture_embedding WHERE picture_id = ?", pictureId);
    }

    /**
     * 检索与查询向量最相似的前 {@code topK} 个图片 ID（按余弦距离升序）。
     *
     * @param embedding 查询向量（512 维）
     * @param spaceId   限定空间 ID（null 表示仅公共图库，即 space_id 为空）
     * @param topK      返回数量
     * @return 相似图片 ID 列表（按相似度降序）
     */
    public List<Long> searchSimilarPictureIds(List<Float> embedding, Long spaceId, int topK) {
        String vector = toVectorString(embedding);
        if (spaceId == null) {
            // 公共图库：space_id 为空
            String sql = "SELECT picture_id FROM picture_embedding "
                    + "WHERE space_id IS NULL ORDER BY embedding <=> CAST(? AS vector) LIMIT ?";
            return jdbcTemplate.queryForList(sql, Long.class, vector, topK);
        }
        String sql = "SELECT picture_id FROM picture_embedding "
                + "WHERE space_id = ? ORDER BY embedding <=> CAST(? AS vector) LIMIT ?";
        return jdbcTemplate.queryForList(sql, Long.class, spaceId, vector, topK);
    }

    /**
     * 将向量列表转换为 pgvector 字符串字面量 {@code [x1,x2,...]}。
     */
    private String toVectorString(List<Float> embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(embedding.get(i));
        }
        return sb.append(']').toString();
    }
}
