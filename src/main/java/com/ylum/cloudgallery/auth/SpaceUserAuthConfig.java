package com.ylum.cloudgallery.auth;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 空间权限配置对象，对应 classpath 下的 spaceUserAuthConfig.json。
 *
 * <p>通过「配置化」方式描述角色与权限码的映射关系，
 * 使权限调整无需改动代码，只需修改 JSON 配置文件。</p>
 */
@Data
public class SpaceUserAuthConfig {

    /** 权限码 -> 权限说明 */
    private Map<String, String> permissions;

    /** 角色 -> 权限码列表（viewer / editor / admin） */
    private Map<String, List<String>> rolePermissionMap;

    /** 公共图库权限（所有登录用户可用） */
    private List<String> publicPermissions;
}
