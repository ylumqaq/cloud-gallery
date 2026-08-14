package com.ylum.cloudgallery.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.ylum.cloudgallery.model.dto.UserLoginRequest;
import com.ylum.cloudgallery.model.dto.UserRegisterRequest;
import com.ylum.cloudgallery.model.entity.User;
import com.ylum.cloudgallery.model.vo.LoginUserVO;
import com.ylum.cloudgallery.model.vo.UserVO;

/**
 * 用户服务接口，定义注册 / 登录 / 获取当前用户 / 退出等能力。
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册。
     *
     * @param request 注册请求参数
     * @return 新注册用户的 ID
     */
    long userRegister(UserRegisterRequest request);

    /**
     * 用户登录，成功后登录态写入 Redis。
     *
     * @param request 登录请求参数
     * @return 用户脱敏信息 + token
     */
    LoginUserVO userLogin(UserLoginRequest request);

    /**
     * 获取当前登录用户的脱敏信息。
     *
     * @return 当前用户脱敏视图对象
     */
    UserVO getLoginUser();

    /**
     * 退出登录，清除当前会话。
     */
    void userLogout();

    /**
     * 修改指定用户的角色（仅高级管理员可调用）。
     *
     * @param targetUserId 目标用户 ID
     * @param userRole     目标角色（user / admin / super_admin）
     */
    void updateUserRole(Long targetUserId, String userRole);
}
