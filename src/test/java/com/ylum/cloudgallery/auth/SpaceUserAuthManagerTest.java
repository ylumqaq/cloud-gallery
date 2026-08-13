package com.ylum.cloudgallery.auth;

import com.ylum.cloudgallery.common.BusinessException;
import com.ylum.cloudgallery.constant.SpaceUserConstant;
import com.ylum.cloudgallery.mapper.SpaceMapper;
import com.ylum.cloudgallery.mapper.SpaceUserMapper;
import com.ylum.cloudgallery.model.entity.Space;
import com.ylum.cloudgallery.model.entity.SpaceUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 空间权限管理器单元测试：覆盖公共图库 / 私有空间 / 团队空间三种场景的权限收敛，
 * 以及角色查询逻辑（对应测试文档 TC-PERM-001 ~ TC-PERM-006）。
 */
@ExtendWith(MockitoExtension.class)
class SpaceUserAuthManagerTest {

    @Mock
    private SpaceMapper spaceMapper;

    @Mock
    private SpaceUserMapper spaceUserMapper;

    @InjectMocks
    private SpaceUserAuthManager manager;

    /**
     * 每个用例前加载 classpath 下的 spaceUserAuthConfig.json 配置。
     */
    @BeforeEach
    void setUp() throws IOException {
        manager.init();
    }

    /**
     * 构造空间实体。
     */
    private Space buildSpace(long id, long ownerId, int spaceType) {
        Space space = new Space();
        space.setId(id);
        space.setUserId(ownerId);
        space.setSpaceType(spaceType);
        return space;
    }

    /**
     * 构造空间成员实体。
     */
    private SpaceUser buildSpaceUser(long spaceId, long userId, String role) {
        SpaceUser spaceUser = new SpaceUser();
        spaceUser.setSpaceId(spaceId);
        spaceUser.setUserId(userId);
        spaceUser.setSpaceRole(role);
        return spaceUser;
    }

    @Test
    void getPermissionList_publicSpace_returnsPublicPermissions() {
        List<String> permissions = manager.getPermissionList(null, 1L);

        assertTrue(permissions.contains(SpaceUserConstant.PERMISSION_PICTURE_VIEW));
        assertTrue(permissions.contains(SpaceUserConstant.PERMISSION_PICTURE_UPLOAD));
        assertFalse(permissions.contains(SpaceUserConstant.PERMISSION_PICTURE_DELETE));
    }

    @Test
    void getPermissionList_privateSpace_creator_returnsAdminPermissions() {
        when(spaceMapper.selectById(100L)).thenReturn(buildSpace(100L, 1L, 0));

        List<String> permissions = manager.getPermissionList(100L, 1L);

        assertTrue(permissions.contains(SpaceUserConstant.PERMISSION_PICTURE_DELETE));
        assertTrue(permissions.contains(SpaceUserConstant.PERMISSION_SPACE_USER_MANAGE));
    }

    @Test
    void getPermissionList_privateSpace_nonCreator_returnsEmpty() {
        when(spaceMapper.selectById(100L)).thenReturn(buildSpace(100L, 1L, 0));

        List<String> permissions = manager.getPermissionList(100L, 2L);

        assertTrue(permissions.isEmpty());
    }

    @Test
    void getPermissionList_teamSpace_viewer_returnsViewOnly() {
        when(spaceMapper.selectById(100L)).thenReturn(buildSpace(100L, 1L, 1));
        when(spaceUserMapper.selectOne(any())).thenReturn(buildSpaceUser(100L, 2L, SpaceUserConstant.ROLE_VIEWER));

        List<String> permissions = manager.getPermissionList(100L, 2L);

        assertEquals(List.of(SpaceUserConstant.PERMISSION_PICTURE_VIEW), permissions);
    }

    @Test
    void getPermissionList_teamSpace_editor_returnsEditPermissions() {
        when(spaceMapper.selectById(100L)).thenReturn(buildSpace(100L, 1L, 1));
        when(spaceUserMapper.selectOne(any())).thenReturn(buildSpaceUser(100L, 2L, SpaceUserConstant.ROLE_EDITOR));

        List<String> permissions = manager.getPermissionList(100L, 2L);

        assertTrue(permissions.contains(SpaceUserConstant.PERMISSION_PICTURE_EDIT));
        assertFalse(permissions.contains(SpaceUserConstant.PERMISSION_SPACE_USER_MANAGE));
    }

    @Test
    void getPermissionList_teamSpace_admin_returnsManagePermission() {
        when(spaceMapper.selectById(100L)).thenReturn(buildSpace(100L, 1L, 1));
        when(spaceUserMapper.selectOne(any())).thenReturn(buildSpaceUser(100L, 2L, SpaceUserConstant.ROLE_ADMIN));

        List<String> permissions = manager.getPermissionList(100L, 2L);

        assertTrue(permissions.contains(SpaceUserConstant.PERMISSION_SPACE_USER_MANAGE));
    }

    @Test
    void getPermissionList_teamSpace_nonMember_returnsEmpty() {
        when(spaceMapper.selectById(100L)).thenReturn(buildSpace(100L, 1L, 1));
        when(spaceUserMapper.selectOne(any())).thenReturn(null);

        List<String> permissions = manager.getPermissionList(100L, 2L);

        assertTrue(permissions.isEmpty());
    }

    @Test
    void getPermissionList_spaceNotExist_throws() {
        when(spaceMapper.selectById(100L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> manager.getPermissionList(100L, 1L));
    }

    @Test
    void getRole_creator_returnsCreator() {
        when(spaceMapper.selectById(100L)).thenReturn(buildSpace(100L, 1L, 0));

        assertEquals("creator", manager.getRole(100L, 1L));
    }

    @Test
    void getRole_publicSpace_returnsNull() {
        assertNull(manager.getRole(null, 1L));
    }

    @Test
    void getRole_teamMember_returnsRole() {
        when(spaceMapper.selectById(100L)).thenReturn(buildSpace(100L, 1L, 1));
        when(spaceUserMapper.selectOne(any())).thenReturn(buildSpaceUser(100L, 2L, SpaceUserConstant.ROLE_VIEWER));

        assertEquals(SpaceUserConstant.ROLE_VIEWER, manager.getRole(100L, 2L));
    }
}
