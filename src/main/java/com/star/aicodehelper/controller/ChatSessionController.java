package com.star.aicodehelper.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.star.aicodehelper.common.BaseResponse;
import com.star.aicodehelper.common.ResultUtils;
import com.star.aicodehelper.model.entity.ChatSession;
import com.star.aicodehelper.model.entity.User;
import com.star.aicodehelper.service.ChatSessionService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/session")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ChatSessionController {

    @Resource
    private ChatSessionService chatSessionService;

    @PostMapping("/create")
    public ChatSession createSession(@RequestBody ChatSession chatSession, HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute("user_login");
        if (loginUser == null) {
            throw new RuntimeException("未登录");
        }
        chatSession.setUserId(loginUser.getId());
        chatSession.setCreateTime(new Date());
        chatSession.setUpdateTime(new Date());
        if (chatSession.getName() == null || chatSession.getName().isEmpty()) {
            chatSession.setName("新会话 " + new Date().toString());
        }
        chatSessionService.save(chatSession);
        return chatSession;
    }

    @GetMapping("/list")
    public List<ChatSession> listSessions(@RequestParam String assistantType, HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute("user_login");
        if (loginUser == null) {
            throw new RuntimeException("未登录");
        }
        LambdaQueryWrapper<ChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatSession::getUserId, loginUser.getId());
        queryWrapper.eq(ChatSession::getAssistantType, assistantType);
        queryWrapper.orderByDesc(ChatSession::getUpdateTime);
        return chatSessionService.list(queryWrapper);
    }

    @PostMapping("/rename")
    public boolean renameSession(@RequestBody ChatSession chatSession) {
        ChatSession session = chatSessionService.getById(chatSession.getId());
        if (session != null) {
            session.setName(chatSession.getName());
            session.setUpdateTime(new Date());
            return chatSessionService.updateById(session);
        }
        return false;
    }

    @PostMapping("/delete")
    public boolean deleteSession(@RequestBody ChatSession chatSession) {
        return chatSessionService.removeById(chatSession.getId());
    }
}
