package com.ylum.cloudgallery.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 用户角色常量单元测试：覆盖管理员权限判定（hasAdminRole）与角色合法性校验（isLegalRole）。
 */
class UserConstantTest {

    /**
     * admin 应具备管理员权限。
     */
    @Test
    void hasAdminRole_admin_returnsTrue() {
        assertTrue(UserConstant.hasAdminRole(UserConstant.ADMIN_ROLE));
    }

    /**
     * super_admin 应继承管理员权限。
     */
    @Test
    void hasAdminRole_superAdmin_returnsTrue() {
        assertTrue(UserConstant.hasAdminRole(UserConstant.SUPER_ADMIN_ROLE));
    }

    /**
     * 普通用户不应具备管理员权限。
     */
    @Test
    void hasAdminRole_user_returnsFalse() {
        assertFalse(UserConstant.hasAdminRole(UserConstant.USER_ROLE));
    }

    /**
     * 空角色不应被判定为管理员。
     */
    @Test
    void hasAdminRole_null_returnsFalse() {
        assertFalse(UserConstant.hasAdminRole(null));
    }

    /**
     * user / admin / super_admin 三种角色均合法。
     */
    @Test
    void isLegalRole_knownRoles_returnsTrue() {
        assertTrue(UserConstant.isLegalRole(UserConstant.USER_ROLE));
        assertTrue(UserConstant.isLegalRole(UserConstant.ADMIN_ROLE));
        assertTrue(UserConstant.isLegalRole(UserConstant.SUPER_ADMIN_ROLE));
    }

    /**
     * 未知角色与空值均不合法。
     */
    @Test
    void isLegalRole_unknownRole_returnsFalse() {
        assertFalse(UserConstant.isLegalRole("root"));
        assertFalse(UserConstant.isLegalRole(null));
    }
}
