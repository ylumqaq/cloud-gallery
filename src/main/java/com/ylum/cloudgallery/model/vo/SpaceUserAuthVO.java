package com.ylum.cloudgallery.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 当前用户在指定空间中的权限信息。
 */
@Data
@Schema(description = "当前用户在指定空间的角色与权限")
public class SpaceUserAuthVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 角色：creator（创建者）/ viewer / editor / admin，公共图库或非成员为 null */
    @Schema(description = "角色：creator / viewer / editor / admin")
    private String role;

    /** 权限码列表 */
    @Schema(description = "权限码列表")
    private List<String> permissions;
}
