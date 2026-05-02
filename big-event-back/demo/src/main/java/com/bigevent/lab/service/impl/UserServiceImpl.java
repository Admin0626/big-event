package com.bigevent.lab.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bigevent.lab.common.Result;
import com.bigevent.lab.entity.User;
import com.bigevent.lab.mapper.UserMapper;
import com.bigevent.lab.service.UserService;
import com.bigevent.lab.utils.JwtUtils;
import com.bigevent.lab.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户Service实现类
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 常量：密码字段名
     */
    private static final String FIELD_PASSWORD = "password";

    /**
     * 用户注册
     * 1. 校验用户名是否已存在
     * 2. MD5加密密码
     * 3. 入库
     */
    @Override
    public void register(String username, String password) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        Long count = userMapper.selectCount(queryWrapper);

        if (count > 0) {
            throw new RuntimeException("用户名已被占用");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(md5(password));
        user.setNickname("");
        user.setEmail("");
        user.setAvatarUrl("");
        userMapper.insert(user);
    }

    /**
     * 用户登录
     * 1. 根据用户名查询
     * 2. 校验密码
     * 3. 生成JWT返回
     */
    @Override
    public String login(String username, String password) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new RuntimeException("用户名不存在");
        }

        if (!md5(password).equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        return jwtUtils.generateToken(claims);
    }

    /**
     * 根据ID获取用户信息
     */
    @Override
    public User getUserById(Integer userId) {
        return userMapper.selectById(userId);
    }

    /**
     * 更新用户昵称和邮箱
     */
    @Override
    public void updateUser(User user) {
        User existingUser = userMapper.selectById(user.getId());
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }

        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setNickname(user.getNickname());
        updateUser.setEmail(user.getEmail());
        userMapper.updateById(updateUser);
    }

    /**
     * 更新头像
     */
    @Override
    public void updateAvatar(Integer userId, String avatarUrl) {
        User user = new User();
        user.setId(userId);
        user.setAvatarUrl(avatarUrl);
        userMapper.updateById(user);
    }

    /**
     * 修改密码
     * 1. 校验旧密码
     * 2. 更新为新密码
     */
    @Override
    public void updatePassword(Integer userId, String oldPwd, String newPwd) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!md5(oldPwd).equals(user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(md5(newPwd));
        userMapper.updateById(updateUser);
    }

    /**
     * MD5加密工具方法
     */
    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5加密失败", e);
        }
    }
}
