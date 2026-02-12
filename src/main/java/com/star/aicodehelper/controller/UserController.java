package com.star.aicodehelper.controller;

import com.star.aicodehelper.common.BaseResponse;
import com.star.aicodehelper.common.ResultUtils;
import com.star.aicodehelper.model.entity.User;
import com.star.aicodehelper.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;
    @PostMapping("/register")
    public User register(@RequestBody User user) {
        if (user == null || user.getUserAccount() == null || user.getUserPassword() == null) {
            throw new RuntimeException("参数错误");
        }
        return userService.register(user.getUserAccount(), user.getUserPassword());
    }

    @PostMapping("/login")
    public User login(@RequestBody User user, HttpServletRequest request) {
        if (user == null || user.getUserAccount() == null || user.getUserPassword() == null) {
            throw new RuntimeException("参数错误");
        }
        return userService.login(user.getUserAccount(), user.getUserPassword(), request);
    }

    @GetMapping("/current")
    public User getCurrentUser(HttpServletRequest request) {
        return userService.getLoginUser(request);
    }

    @PostMapping("/logout")
    public boolean logout(HttpServletRequest request) {
        return userService.logout(request);
    }
}
