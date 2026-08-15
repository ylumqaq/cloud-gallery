package com.ylum.cloudgallery.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 图片实体，对应 MySQL 中的 picture 表。
 *
 * <p>图片元数据存 MySQL，特征向量存 PostgreSQL（阶段 5 落地）。
 * 按 {@code space_id} 分表，公共图库图片的 {@code space_id} 为空。</p>
 */
@Data
@TableName("picture")
public class Picture implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 图片 ID（雪花 ID，作为与向量表的关联键） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 图片名称 */
    private String name;

    /** 原图 URL（COS） */
    private String url;

    /** 缩略图 URL（COS） */
    private String thumbnailUrl;

    /** 图片大小（字节） */
    private Long picSize;

    /** 图片宽度 */
    private Integer picWidth;

    /** 图片高度 */
    private Integer picHeight;

    /** 格式：jpeg / png / jpg / webp */
    private String picFormat;

    /** 平均主色调（用于按颜色搜索） */
    private String picColor;

    /** 图片分类（用于分类分析） */
    private String category;

    /** 图片标签（JSON 数组字符串，用于标签分析） */
    private String tags;

    /** 所属空间 ID（空 = 公共图库） */
    private Long spaceId;

    /** 上传者 / 作者用户 ID */
    private Long userId;

    /** 创建时间（插入时自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 最近编辑时间 */
    private LocalDateTime editTime;

    /** 更新时间（插入与更新时自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除：0 未删除 / 1 已删除 */
    @TableLogic
    private Integer isDelete;
}
