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
 * 空间实体，对应 MySQL 中的 space 表。
 *
 * <p>空间用于组织图片，分为私有空间与团队空间；
 * 公共图库不创建 space 记录（对应 picture.space_id 为空）。</p>
 */
@Data
@TableName("space")
public class Space implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 空间 ID（雪花 ID，应用层生成） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 空间名称 */
    private String spaceName;

    /** 空间类型：0 私有 / 1 团队 */
    private Integer spaceType;

    /** 创建者用户 ID */
    private Long userId;

    /** 创建时间（插入时自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间（插入与更新时自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除：0 未删除 / 1 已删除 */
    @TableLogic
    private Integer isDelete;
}
