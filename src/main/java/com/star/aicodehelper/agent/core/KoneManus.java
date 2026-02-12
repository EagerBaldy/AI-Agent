package com.star.aicodehelper.agent.core;

import com.star.aicodehelper.service.AgentMessageService;
import com.star.aicodehelper.ai.tools.AmapTool;
import com.star.aicodehelper.ai.tools.WebSearchTool;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.stereotype.Component;

@Component
public class KoneManus extends ToolCallAgent {

    private final WebSearchTool webSearchTool;
    private final AmapTool amapTool;

    public KoneManus(ChatModel myQwenChatModel, 
                     WebSearchTool webSearchTool, 
                     AmapTool amapTool,
                     AgentMessageService agentMessageService) {
        super(myQwenChatModel);
        this.webSearchTool = webSearchTool;
        this.amapTool = amapTool;
        this.setAgentMessageService(agentMessageService);
        // 重新注册以确保依赖注入后工具可用
        registerTools(); 
    }

    @Override
    protected void registerTools() {
        // 防止 webSearchTool 在 super 构造函数调用时为空
        if (webSearchTool == null) return;
        
        addTool("Search", "Useful for when you need to answer questions about current events or technical details. Input should be a search query.", 
            query -> {
                // 适配 WebSearchTool 的输出
                return webSearchTool.searchWeb(query);
            }
        );

        if (amapTool != null) {
            addTool("Weather", "Useful for checking weather conditions in a specific city. Input should be the city name (e.g., 'Shanghai', 'Beijing').", 
                city -> amapTool.getWeather(city)
            );
        }
    }
}
