package com.ylum.cloudgallery.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.ylum.cloudgallery.common.BusinessException;
import com.ylum.cloudgallery.common.ErrorCode;
import com.ylum.cloudgallery.constant.UserConstant;
import com.ylum.cloudgallery.mapper.UserMapper;
import com.ylum.cloudgallery.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 用户服务实现单元测试：重点覆盖修改用户角色（updateUserRole）的安全校验分支。
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    private UserServiceImpl userService;

    /**
     * 每个用例前手动构造 service 并注入 baseMapper，避免依赖 Spring 容器。
     */
    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl();
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
    }

    /**
     * 构造指定角色与 ID 的用户实体。
     */
    private User buildUser(long id, String role) {
        User user = new User();
        user.setId(id);
        user.setUserRole(role);
        return user;
    }

    /**
     * 目标角色非法时应抛出参数错误。
     */
    @Test
    void updateUserRole_invalidRole_throwsParamsError() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> userService.updateUserRole(2L, "root"));

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), e.getCode());
    }

    /**
     * 操作者不是高级管理员时应抛出无权限错误。
     */
    @Test
    void updateUserRole_operatorNotSuperAdmin_throwsNoAuth() {
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginIdAsLong).thenReturn(1L);
            when(userMapper.selectById(1L)).thenReturn(buildUser(1L, UserConstant.ADMIN_ROLE));

            BusinessException e = assertThrows(BusinessException.class,
                    () -> userService.updateUserRole(2L, UserConstant.ADMIN_ROLE));

            assertEquals(ErrorCode.NO_AUTH_ERROR.getCode(), e.getCode());
        }
    }

    /**
     * 高级管理员修改自己的角色时应抛出参数错误。
     */
    @Test
    void updateUserRole_modifySelf_throwsParamsError() {
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginIdAsLong).thenReturn(1L);
            when(userMapper.selectById(1L)).thenReturn(buildUser(1L, UserConstant.SUPER_ADMIN_ROLE));

            BusinessException e = assertThrows(BusinessException.class,
                    () -> userService.updateUserRole(1L, UserConstant.ADMIN_ROLE));

            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), e.getCode());
        }
    }

    /**
     * 目标用户不存在时应抛出数据不存在错误。
     */
    @Test
    void updateUserRole_targetNotExist_throwsNotFound() {
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginIdAsLong).thenReturn(1L);
            when(userMapper.selectById(1L)).thenReturn(buildUser(1L, UserConstant.SUPER_ADMIN_ROLE));
            when(userMapper.selectById(2L)).thenReturn(null);

            BusinessException e = assertThrows(BusinessException.class,
                    () -> userService.updateUserRole(2L, UserConstant.ADMIN_ROLE));

            assertEquals(ErrorCode.NOT_FOUND_ERROR.getCode(), e.getCode());
        }
    }

    /**
     * 移除最后一个高级管理员时应抛出操作失败错误。
     */
    @Test
    void updateUserRole_removeLastSuperAdmin_throwsOperationError() {
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginIdAsLong).thenReturn(1L);
            when(userMapper.selectById(1L)).thenReturn(buildUser(1L, UserConstant.SUPER_ADMIN_ROLE));
            when(userMapper.selectById(2L)).thenReturn(buildUser(2L, UserConstant.SUPER_ADMIN_ROLE));
            when(userMapper.selectCount(any())).thenReturn(1L);

            BusinessException e = assertThrows(BusinessException.class,
                    () -> userService.updateUserRole(2L, UserConstant.USER_ROLE));

            assertEquals(ErrorCode.OPERATION_ERROR.getCode(), e.getCode());
        }
    }

    /**
     * 合法修改时应成功更新目标用户角色。
     */
    @Test
    void updateUserRole_success() {
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginIdAsLong).thenReturn(1L);
            when(userMapper.selectById(1L)).thenReturn(buildUser(1L, UserConstant.SUPER_ADMIN_ROLE));
            when(userMapper.selectById(2L)).thenReturn(buildUser(2L, UserConstant.USER_ROLE));
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            userService.updateUserRole(2L, UserConstant.ADMIN_ROLE);

            verify(userMapper).updateById(any(User.class));
        }
    }
}
