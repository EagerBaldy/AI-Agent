package com.star.aicodehelper.controller;

import com.star.aicodehelper.common.BaseResponse;
import com.star.aicodehelper.common.ResultUtils;
import com.star.aicodehelper.model.entity.ChatMessage;
import com.star.aicodehelper.model.entity.User;
import com.star.aicodehelper.service.ChatHistoryService;
import com.star.aicodehelper.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UserService userService;

    @GetMapping("/history")
    public List<ChatMessage> getHistory(
            @RequestParam Long memoryId,
            @RequestParam String assistantType,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new RuntimeException("未登录");
        }
        return chatHistoryService.listMessages(loginUser.getId(), memoryId, assistantType);
    }
    
    @PostMapping("/history")
    public boolean saveMessage(
            @RequestBody ChatMessage chatMessage,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new RuntimeException("未登录");
        }
        chatMessage.setUserId(loginUser.getId());
        
        // 如果 assistantType 为空，设置默认值
        if (chatMessage.getAssistantType() == null || chatMessage.getAssistantType().isEmpty()) {
            chatMessage.setAssistantType("code"); // 默认为 code
        }
        
        // 如果 role 为空，设置默认值
        if (chatMessage.getRole() == null || chatMessage.getRole().isEmpty()) {
            chatMessage.setRole(Boolean.TRUE.equals(chatMessage.getIsUser()) ? "user" : "ai");
        }
        
        // 确保 content 不为 null，避免数据库报错
        if (chatMessage.getContent() == null) {
            chatMessage.setContent("");
        }
        
        return chatHistoryService.saveMessage(chatMessage);
    }
}
