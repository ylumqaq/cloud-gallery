-- 云图库 MySQL 业务库建表脚本
-- 使用前提：已创建数据库 cloud_gallery（utf8mb4），命令如下：
--   CREATE DATABASE cloud_gallery DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
--
-- 主键策略说明：统一使用 BIGINT + 雪花 ID（应用层 MyBatis-Plus ASSIGN_ID 生成），
-- 因此主键不使用 AUTO_INCREMENT。

-- 1. user 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id`            BIGINT        NOT NULL COMMENT '用户 ID（雪花 ID）',
    `user_account`  VARCHAR(256)  NOT NULL COMMENT '登录账号',
    `user_password` VARCHAR(512)  NOT NULL COMMENT '密码（加密存储）',
    `user_name`     VARCHAR(256)  NULL     COMMENT '昵称',
    `user_avatar`   VARCHAR(1024) NULL     COMMENT '头像 URL',
    `user_profile`  VARCHAR(512)  NULL     COMMENT '个人简介',
    `user_role`     VARCHAR(64)   NOT NULL DEFAULT 'user' COMMENT '用户角色（user / admin / super_admin）',
    `create_time`   DATETIME      NOT NULL COMMENT '创建时间',
    `update_time`   DATETIME      NOT NULL COMMENT '更新时间',
    `is_delete`     TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除 / 1 已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_account` (`user_account`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户表';

-- 2. space 空间表
CREATE TABLE IF NOT EXISTS `space` (
    `id`          BIGINT       NOT NULL COMMENT '空间 ID（雪花 ID）',
    `space_name`  VARCHAR(128) NOT NULL COMMENT '空间名称',
    `space_type`  TINYINT      NOT NULL COMMENT '空间类型：0 私有 / 1 团队',
    `user_id`     BIGINT       NOT NULL COMMENT '创建者用户 ID',
    `create_time` DATETIME     NOT NULL COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL COMMENT '更新时间',
    `is_delete`   TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除 / 1 已删除',
    PRIMARY KEY (`id`),
    KEY `idx_space_user_id` (`user_id`),
    KEY `idx_space_type` (`space_type`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '空间表';

-- 3. space_user 空间成员表（成员关系为物理删除，无 is_delete 字段）
CREATE TABLE IF NOT EXISTS `space_user` (
    `id`          BIGINT      NOT NULL COMMENT '成员关系 ID（雪花 ID）',
    `space_id`    BIGINT      NOT NULL COMMENT '空间 ID',
    `user_id`     BIGINT      NOT NULL COMMENT '用户 ID',
    `space_role`  VARCHAR(64) NOT NULL COMMENT '角色：viewer / editor / admin',
    `create_time` DATETIME    NOT NULL COMMENT '加入时间',
    `update_time` DATETIME    NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_space_user` (`space_id`, `user_id`),
    KEY `idx_space_user_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '空间成员表';

-- 4. picture 图片表（逻辑表，后续阶段按 space_id 分表）
CREATE TABLE IF NOT EXISTS `picture` (
    `id`            BIGINT        NOT NULL COMMENT '图片 ID（雪花 ID，作为与向量表的关联键）',
    `name`          VARCHAR(128)  NOT NULL COMMENT '图片名称',
    `url`           VARCHAR(1024) NOT NULL COMMENT '原图 URL（COS）',
    `thumbnail_url` VARCHAR(1024) NULL     COMMENT '缩略图 URL（COS）',
    `pic_size`      BIGINT        NULL     COMMENT '图片大小（字节）',
    `pic_width`     INT           NULL     COMMENT '图片宽度',
    `pic_height`    INT           NULL     COMMENT '图片高度',
    `pic_format`    VARCHAR(32)   NULL     COMMENT '格式：jpeg / png / jpg / webp',
    `pic_color`     VARCHAR(32)   NULL     COMMENT '平均主色调（用于按颜色搜索）',
    `category`      VARCHAR(128)  NULL     COMMENT '图片分类（用于分类分析）',
    `tags`          VARCHAR(512)  NULL     COMMENT '图片标签（JSON 数组字符串，用于标签分析）',
    `space_id`      BIGINT        NULL     COMMENT '所属空间 ID（空 = 公共图库）',
    `user_id`       BIGINT        NOT NULL COMMENT '上传者 / 作者用户 ID',
    `create_time`   DATETIME      NOT NULL COMMENT '创建时间',
    `edit_time`     DATETIME      NULL     COMMENT '最近编辑时间',
    `update_time`   DATETIME      NOT NULL COMMENT '更新时间',
    `is_delete`     TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除 / 1 已删除',
    PRIMARY KEY (`id`),
    KEY `idx_picture_space_id` (`space_id`),
    KEY `idx_picture_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '图片表';

-- 说明：阶段 7 为 picture 表新增 category / tags 字段（用于空间分析）。
-- 新空间通过 `CREATE TABLE picture_{spaceId} LIKE picture` 建表会自动继承新字段；
-- 对已存在的分表需手动执行迁移（示例）：
--   ALTER TABLE `picture_1` ADD COLUMN `category` VARCHAR(128) NULL COMMENT '图片分类（用于分类分析）' AFTER `pic_color`,
--                          ADD COLUMN `tags` VARCHAR(512) NULL COMMENT '图片标签（JSON 数组字符串，用于标签分析）' AFTER `category`;
