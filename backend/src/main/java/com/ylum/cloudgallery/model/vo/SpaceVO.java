package com.ylum.cloudgallery.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 空间视图对象。
 */
@Data
@Schema(description = "空间信息")
public class SpaceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 空间 ID */
    @Schema(description = "空间 ID")
    private Long id;

    /** 空间名称 */
    @Schema(description = "空间名称")
    private String spaceName;

    /** 空间类型：0 私有 / 1 团队 */
    @Schema(description = "空间类型：0 私有 / 1 团队")
    private Integer spaceType;

    /** 创建者用户 ID */
    @Schema(description = "创建者用户 ID")
    private Long userId;

    /** 创建时间 */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
