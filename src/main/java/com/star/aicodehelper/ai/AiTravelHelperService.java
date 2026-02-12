package com.star.aicodehelper.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

/**
 * AI 旅游规划助手服务
 */
public interface AiTravelHelperService {

    @SystemMessage(fromResource = "system-prompt-travel.txt")
    String chat(String userMessage);

    // 流式对话
    @SystemMessage(fromResource = "system-prompt-travel.txt")
    Flux<String> chatStream(@MemoryId int memoryId, @UserMessage String userMessage);
}
