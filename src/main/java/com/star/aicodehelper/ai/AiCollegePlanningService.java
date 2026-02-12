package com.star.aicodehelper.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface AiCollegePlanningService {
    @SystemMessage(fromResource = "/system-prompt-college.txt")
    String chat(String userMessage);

    @SystemMessage(fromResource = "/system-prompt-college.txt")
    reactor.core.publisher.Flux<String> chatStream(@MemoryId int memoryId, @UserMessage String userMessage);
}
