package com.ylum.cloudgallery.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.ylum.cloudgallery.common.BusinessException;
import com.ylum.cloudgallery.common.ErrorCode;
import com.ylum.cloudgallery.constant.UserConstant;
import com.ylum.cloudgallery.mapper.UserMapper;
import com.ylum.cloudgallery.model.dto.UserLoginRequest;
import com.ylum.cloudgallery.model.dto.UserRegisterRequest;
import com.ylum.cloudgallery.model.entity.User;
import com.ylum.cloudgallery.model.vo.LoginUserVO;
import com.ylum.cloudgallery.model.vo.UserVO;
import com.ylum.cloudgallery.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 用户服务实现：注册 / 登录 / 获取当前用户 / 退出。
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 用户注册：校验两次密码一致与账号唯一性，密码加密后入库。
     */
    @Override
    public long userRegister(UserRegisterRequest request) {
        String userAccount = request.getUserAccount();
        String userPassword = request.getUserPassword();
        String checkPassword = request.getCheckPassword();

        // 两次密码不一致，直接拦截
        if (!Objects.equals(userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        // 账号唯一性校验
        Long count = this.lambdaQuery().eq(User::getUserAccount, userAccount).count();
        if (count != null && count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
        }

        // 密码加密存储（BCrypt，加盐哈希）
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(BCrypt.hashpw(userPassword));
        user.setUserName(userAccount);
        user.setUserRole(UserConstant.USER_ROLE);

        boolean saved = this.save(user);
        if (!saved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
        }
        return user.getId();
    }

    /**
     * 用户登录：按账号查询并校验密码，成功后调用 {@link StpUtil#login(Object)} 写入登录态。
     */
    @Override
    public LoginUserVO userLogin(UserLoginRequest request) {
        String userAccount = request.getUserAccount();
        String userPassword = request.getUserPassword();

        User user = this.lambdaQuery().eq(User::getUserAccount, userAccount).one();
        // 账号或密码错误统一提示，避免暴露账号是否存在
        if (user == null || !BCrypt.checkpw(userPassword, user.getUserPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
        }

        // 登录：登录态写入 Redis（由 Sa-Token + Spring Session 托管）
        StpUtil.login(user.getId());

        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        loginUserVO.setToken(StpUtil.getTokenValue());
        return loginUserVO;
    }

    /**
     * 获取当前登录用户：未登录时由全局异常处理器统一转为 40100。
     */
    @Override
    public UserVO getLoginUser() {
        long userId = StpUtil.getLoginIdAsLong();
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 退出登录：清除当前会话。
     */
    @Override
    public void userLogout() {
        StpUtil.logout();
    }

    /**
     * 修改指定用户的角色。
     *
     * <p>仅高级管理员（super_admin）可调用。安全限制：</p>
     * <ul>
     *     <li>目标角色必须是 user / admin / super_admin 三种合法值之一；</li>
     *     <li>禁止修改自己的角色，避免误降级导致系统失去高级管理员；</li>
     *     <li>禁止移除系统中最后一个高级管理员。</li>
     * </ul>
     */
    @Override
    public void updateUserRole(Long targetUserId, String userRole) {
        // 目标角色合法性白名单校验，拦截非法角色值
        if (!UserConstant.isLegalRole(userRole)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "目标角色不合法");
        }

        // 校验当前操作者必须是高级管理员
        long currentUserId = StpUtil.getLoginIdAsLong();
        User currentUser = this.getById(currentUserId);
        if (currentUser == null || !UserConstant.SUPER_ADMIN_ROLE.equals(currentUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "仅高级管理员可修改用户角色");
        }

        // 禁止修改自己的角色，防止高级管理员误操作把自己降级
        if (Objects.equals(currentUserId, targetUserId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能修改自己的角色");
        }

        // 目标用户必须存在
        User targetUser = this.getById(targetUserId);
        if (targetUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "目标用户不存在");
        }

        // 若目标用户当前是高级管理员且正被降级，需保证系统中至少还保留一个高级管理员
        if (UserConstant.SUPER_ADMIN_ROLE.equals(targetUser.getUserRole())
                && !UserConstant.SUPER_ADMIN_ROLE.equals(userRole)) {
            long superAdminCount = this.count(new LambdaQueryWrapper<User>()
                    .eq(User::getUserRole, UserConstant.SUPER_ADMIN_ROLE));
            if (superAdminCount <= 1) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "系统至少保留一个高级管理员");
            }
        }

        targetUser.setUserRole(userRole);
        boolean updated = this.updateById(targetUser);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "修改用户角色失败");
        }
    }
}
