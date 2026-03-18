package com.star.aicodehelper.controller;

import com.star.aicodehelper.common.BaseResponse;
import com.star.aicodehelper.common.ResultUtils;
import com.star.aicodehelper.model.entity.User;
import com.star.aicodehelper.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Tag(name = "用户模块")
public class UserController {

    @Resource
    private UserService userService;
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public User register(@RequestBody User user) {
        if (user == null || user.getUserAccount() == null || user.getUserPassword() == null) {
            throw new RuntimeException("参数错误");
        }
        return userService.register(user.getUserAccount(), user.getUserPassword());
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public User login(@RequestBody User user, HttpServletRequest request) {
        if (user == null || user.getUserAccount() == null || user.getUserPassword() == null) {
            throw new RuntimeException("参数错误");
        }
        return userService.login(user.getUserAccount(), user.getUserPassword(), request);
    }

    @GetMapping("/current")
    @Operation(summary = "获取当前用户")
    public User getCurrentUser(HttpServletRequest request) {
        return userService.getLoginUser(request);
    }

    @PostMapping("/logout")
    @Operation(summary = "注销登录")
    public boolean logout(HttpServletRequest request) {
        return userService.logout(request);
    }
}
