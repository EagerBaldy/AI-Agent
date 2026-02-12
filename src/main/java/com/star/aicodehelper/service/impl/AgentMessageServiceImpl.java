package com.star.aicodehelper.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.star.aicodehelper.mapper.AgentMessageMapper;
import com.star.aicodehelper.model.entity.AgentMessage;
import com.star.aicodehelper.service.AgentMessageService;
import org.springframework.stereotype.Service;

@Service
public class AgentMessageServiceImpl extends ServiceImpl<AgentMessageMapper, AgentMessage>
    implements AgentMessageService {
}
