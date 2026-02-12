package com.star.aicodehelper.agent.core;

import com.star.aicodehelper.agent.model.AgentContext;
import dev.langchain4j.model.chat.ChatModel;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public abstract class BaseAgent {

    protected final ChatModel chatModel;

    public BaseAgent(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * 执行任务，返回流式输出
     * @param input 用户输入
     * @return Flux流，包含过程信息
     */
    public abstract Flux<String> execute(String input);

    /**
     * 执行任务，带用户和会话上下文
     */
    public Flux<String> execute(String input, Long userId, Long sessionId) {
        return execute(input);
    }

    protected AgentContext initContext(String input) {
        return AgentContext.builder()
                .userInput(input)
                .isFinished(false)
                .build();
    }
}
