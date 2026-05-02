package com.bigevent.lab.controller;

import com.bigevent.lab.common.Result;
import com.bigevent.lab.entity.User;
import com.bigevent.lab.service.UserService;
import com.bigevent.lab.utils.ThreadLocalUtil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * 处理用户注册、登录、信息获取、修改等接口
 */
@RestController
@RequestMapping("/user")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * POST /user/register (x-www-form-urlencoded)
     * 用户注册：校验用户名未重复，MD5加密后入库
     */
    @PostMapping("/register")
    public Result<Void> register(
            @NotBlank(message = "用户名不能为空")
            @Size(min = 5, max = 16, message = "用户名长度必须在5~16位之间")
            @RequestParam("username") String username,

            @NotBlank(message = "密码不能为空")
            @Size(min = 5, max = 16, message = "密码长度必须在5~16位之间")
            @RequestParam("password") String password
    ) {
        userService.register(username, password);
        return Result.success();
    }

    /**
     * POST /user/login (x-www-form-urlencoded)
     * 用户登录：校验用户名密码，匹配成功生成JWT
     */
    @PostMapping("/login")
    public Result<String> login(
            @NotBlank(message = "用户名不能为空")
            @RequestParam("username") String username,

            @NotBlank(message = "密码不能为空")
            @RequestParam("password") String password
    ) {
        String token = userService.login(username, password);
        return Result.success(token);
    }

    /**
     * GET /user/userInfo
     * 获取当前登录用户信息（从ThreadLocal获取userId）
     */
    @GetMapping("/userInfo")
    public Result<User> userInfo() {
        Integer userId = ThreadLocalUtil.get("userId");
        User user = userService.getUserById(userId);
        return Result.success(user);
    }

    /**
     * PUT /user/update (application/json)
     * 更新用户昵称和邮箱
     */
    @PutMapping("/update")
    public Result<Void> update(@Validated @RequestBody UpdateUserDTO dto) {
        Integer userId = ThreadLocalUtil.get("userId");
        User user = new User();
        user.setId(userId);
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        userService.updateUser(user);
        return Result.success();
    }

    /**
     * PATCH /user/updateAvatar (query string)
     * 更新用户头像
     */
    @PatchMapping("/updateAvatar")
    public Result<Void> updateAvatar(@RequestParam("avatarUrl") String avatarUrl) {
        Integer userId = ThreadLocalUtil.get("userId");
        userService.updateAvatar(userId, avatarUrl);
        return Result.success();
    }

    /**
     * PATCH /user/updatePwd (application/json)
     * 修改密码：校验旧密码，新密码与确认密码一致
     */
    @PatchMapping("/updatePwd")
    public Result<Void> updatePwd(@Validated @RequestBody UpdatePwdDTO dto) {
        Integer userId = ThreadLocalUtil.get("userId");
        userService.updatePassword(userId, dto.getOldPwd(), dto.getNewPwd());
        return Result.success();
    }

    /**
     * DTO: 更新用户信息
     */
    @lombok.Data
    public static class UpdateUserDTO {
        @Email(message = "邮箱格式不正确")
        private String email;
        private String nickname;
    }

    /**
     * DTO: 修改密码
     */
    @lombok.Data
    public static class UpdatePwdDTO {
        @NotBlank(message = "旧密码不能为空")
        private String old_pwd;

        @NotBlank(message = "新密码不能为空")
        @Size(min = 5, max = 16, message = "新密码长度必须在5~16位之间")
        private String new_pwd;

        @NotBlank(message = "确认密码不能为空")
        private String re_pwd;

        public String getOldPwd() {
            return old_pwd;
        }

        public String getNewPwd() {
            if (!new_pwd.equals(re_pwd)) {
                throw new RuntimeException("两次输入的密码不一致");
            }
            return new_pwd;
        }
    }
}
