package com.star.aicodehelper.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

/**
 * AI 素材百科服务
 */
public interface AiMaterialEncyclopediaService {

    @SystemMessage(fromResource = "system-prompt-material.txt")
    Flux<String> chatStream(@MemoryId int memoryId, @UserMessage String userMessage);
}
