package com.ylum.cloudgallery.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 空间成员实体，对应 MySQL 中的 space_user 表。
 *
 * <p>承载用户与空间的多对多关系 + 成员角色（RBAC）。
 * 成员关系为物理删除，因此没有 is_delete 字段。</p>
 */
@Data
@TableName("space_user")
public class SpaceUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 成员关系 ID（雪花 ID，应用层生成） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 空间 ID */
    private Long spaceId;

    /** 用户 ID */
    private Long userId;

    /** 角色：viewer / editor / admin */
    private String spaceRole;

    /** 加入时间（插入时自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间（插入与更新时自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
