package com.star.aicodehelper.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.star.aicodehelper.mapper.ChatSessionMapper;
import com.star.aicodehelper.model.entity.ChatSession;
import com.star.aicodehelper.service.ChatSessionService;
import org.springframework.stereotype.Service;

@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession>
    implements ChatSessionService {
}
