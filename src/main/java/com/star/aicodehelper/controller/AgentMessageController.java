package com.star.aicodehelper.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.star.aicodehelper.model.entity.AgentMessage;
import com.star.aicodehelper.model.entity.User;
import com.star.aicodehelper.service.AgentMessageService;
import com.star.aicodehelper.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agent")
public class AgentMessageController {

    @Resource
    private AgentMessageService agentMessageService;

    @Resource
    private UserService userService;

    @PostMapping("/add")
    public boolean addAgentMessage(@RequestBody AgentMessage agentMessage, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new RuntimeException("未登录");
        }
        agentMessage.setUserId(loginUser.getId());
        return agentMessageService.save(agentMessage);
    }

    @GetMapping("/list")
    public List<AgentMessage> listAgentMessages(
            @RequestParam Long sessionId,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new RuntimeException("未登录");
        }
        QueryWrapper<AgentMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", loginUser.getId());
        queryWrapper.eq("session_id", sessionId);
        queryWrapper.orderByAsc("create_time");
        return agentMessageService.list(queryWrapper);
    }
}
