package com.star.aicodehelper.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

/**
 * AI 高考作文名师服务
 */
public interface AiEssayTeacherService {

    @SystemMessage(fromResource = "system-prompt-essay.txt")
    Flux<String> chatStream(@MemoryId int memoryId, @UserMessage String userMessage);
}
