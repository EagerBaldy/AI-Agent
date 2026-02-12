package com.star.aicodehelper.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.star.aicodehelper.mapper.UserMapper;
import com.star.aicodehelper.model.entity.User;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Resource
    private UserMapper userMapper;

    public User register(String userAccount, String userPassword) {
        // 校验账户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        if (userMapper.selectCount(queryWrapper) > 0) {
            throw new RuntimeException("账号已存在");
        }

        // 创建用户
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(userPassword); // 实际生产环境应加密
        userMapper.insert(user);
        return user;
    }

    public User login(String userAccount, String userPassword, HttpServletRequest request) {
        // 查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        queryWrapper.eq("user_password", userPassword);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new RuntimeException("账号或密码错误");
        }

        // 记录登录态
        request.getSession().setAttribute("user_login", user);
        return user;
    }

    public User getLoginUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute("user_login");
    }

    public boolean logout(HttpServletRequest request) {
        request.getSession().removeAttribute("user_login");
        return true;
    }
}
