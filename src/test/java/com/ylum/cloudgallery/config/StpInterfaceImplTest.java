package com.ylum.cloudgallery.config;

import com.ylum.cloudgallery.constant.UserConstant;
import com.ylum.cloudgallery.mapper.UserMapper;
import com.ylum.cloudgallery.model.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Sa-Token 角色 / 权限提供器单元测试。
 */
@ExtendWith(MockitoExtension.class)
class StpInterfaceImplTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private StpInterfaceImpl stpInterface;

    /**
     * 构造用户实体。
     */
    private User buildUser(long id, String role) {
        User user = new User();
        user.setId(id);
        user.setUserRole(role);
        return user;
    }

    /**
     * 用户存在时，getRoleList 应返回该用户角色。
     */
    @Test
    void getRoleList_existingUser_returnsRole() {
        when(userMapper.selectById(1L)).thenReturn(buildUser(1L, UserConstant.SUPER_ADMIN_ROLE));

        List<String> roles = stpInterface.getRoleList(1L, "login");

        assertEquals(List.of(UserConstant.SUPER_ADMIN_ROLE), roles);
    }

    /**
     * 用户不存在时，getRoleList 应返回空列表。
     */
    @Test
    void getRoleList_userNotExist_returnsEmpty() {
        when(userMapper.selectById(99L)).thenReturn(null);

        assertTrue(stpInterface.getRoleList(99L, "login").isEmpty());
    }

    /**
     * 无全局权限码体系，getPermissionList 应返回空列表。
     */
    @Test
    void getPermissionList_returnsEmpty() {
        assertTrue(stpInterface.getPermissionList(1L, "login").isEmpty());
    }
}
