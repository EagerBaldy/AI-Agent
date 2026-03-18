package com.star.aicodehelper.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.star.aicodehelper.mapper.ChatMessageMapper;
import com.star.aicodehelper.model.entity.ChatMessage;
import com.star.aicodehelper.mq.ChatMessageProducer;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatHistoryService {

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private ChatMessageProducer chatMessageProducer;

    @Transactional
    public void saveMessage(Long userId, Long memoryId, String assistantType, String role, String content) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setUserId(userId);
        chatMessage.setMemoryId(memoryId);
        chatMessage.setAssistantType(assistantType);
        chatMessage.setRole(role);
        chatMessage.setContent(content);
        chatMessageMapper.insert(chatMessage);
        
        // 异步发送消息到 MQ
        chatMessageProducer.sendSyncMessage(chatMessage);
    }
    
    @Transactional
    public boolean saveMessage(ChatMessage chatMessage) {
        int result = chatMessageMapper.insert(chatMessage);
        if (result > 0) {
            // 异步发送消息到 MQ
            chatMessageProducer.sendSyncMessage(chatMessage);
            return true;
        }
        return false;
    }

    public List<ChatMessage> listMessages(Long userId, Long memoryId, String assistantType) {
        QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("memory_id", memoryId);
        queryWrapper.eq("assistant_type", assistantType);
        queryWrapper.orderByAsc("create_time");
        return chatMessageMapper.selectList(queryWrapper);
    }
}
