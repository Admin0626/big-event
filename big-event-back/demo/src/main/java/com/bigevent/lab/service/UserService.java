package com.bigevent.lab.service;

import com.bigevent.lab.entity.User;

/**
 * 用户Service接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    void register(String username, String password);

    /**
     * 用户登录，返回JWT Token
     */
    String login(String username, String password);

    /**
     * 根据用户ID获取用户信息
     */
    User getUserById(Integer userId);

    /**
     * 更新用户昵称和邮箱
     */
    void updateUser(User user);

    /**
     * 更新用户头像
     */
    void updateAvatar(Integer userId, String avatarUrl);

    /**
     * 修改密码
     */
    void updatePassword(Integer userId, String oldPwd, String newPwd);
}
