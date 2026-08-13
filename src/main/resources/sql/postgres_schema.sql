-- 云图库 PostgreSQL 向量库建表脚本（pgvector）
-- 使用前提：已安装 pgvector 扩展，目标数据库为 postgres（连接信息：postgres / 123456）

-- 1. 启用 pgvector 扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- 2. picture_embedding 图片向量表
-- 通过 picture_id 与 MySQL picture.id 关联（逻辑外键，无真实外键约束）
CREATE TABLE IF NOT EXISTS picture_embedding (
    picture_id BIGINT       NOT NULL,  -- 对应 MySQL picture.id（主键）
    space_id   BIGINT       NULL,      -- 冗余空间 ID，用于「空间内」过滤
    embedding  vector(512)  NOT NULL,  -- 图片特征向量，维度与 CI 向量特征提取一致
    PRIMARY KEY (picture_id)
);

-- 3. HNSW 向量索引（余弦相似度）
CREATE INDEX ON picture_embedding USING hnsw (embedding vector_cosine_ops);

-- 4. space_id 普通索引
CREATE INDEX idx_picture_embedding_space_id ON picture_embedding (space_id);
