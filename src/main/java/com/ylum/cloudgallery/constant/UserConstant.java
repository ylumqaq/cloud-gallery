package com.ylum.cloudgallery.constant;

/**
 * 用户相关常量。
 */
public interface UserConstant {

    /** 普通用户角色 */
    String USER_ROLE = "user";

    /** 管理员角色 */
    String ADMIN_ROLE = "admin";

    /** 高级管理员角色（超级管理员，可管理其他用户的角色） */
    String SUPER_ADMIN_ROLE = "super_admin";

    /**
     * 判断角色是否具备管理员及以上权限。
     *
     * <p>高级管理员（super_admin）继承管理员（admin）的全部业务权限，
     * 因此本方法对 admin 与 super_admin 均返回 true。</p>
     *
     * @param role 用户角色值
     * @return 是管理员或高级管理员时返回 true，否则返回 false
     */
    static boolean hasAdminRole(String role) {
        return ADMIN_ROLE.equals(role) || SUPER_ADMIN_ROLE.equals(role);
    }

    /**
     * 判断角色值是否为系统合法的用户角色。
     *
     * <p>合法角色仅包括 user / admin / super_admin，用于角色修改接口的白名单校验。</p>
     *
     * @param role 待校验的角色值
     * @return 属于合法角色时返回 true，否则返回 false
     */
    static boolean isLegalRole(String role) {
        return USER_ROLE.equals(role) || ADMIN_ROLE.equals(role) || SUPER_ADMIN_ROLE.equals(role);
    }
}
