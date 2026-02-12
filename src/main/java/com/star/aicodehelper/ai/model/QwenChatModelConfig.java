package com.star.aicodehelper.ai.model;

import com.alibaba.dashscope.protocol.ConnectionConfigurations;
import com.alibaba.dashscope.utils.Constants;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "langchain4j.community.dashscope.chat-model")
@Data
public class QwenChatModelConfig {

    private String apiKey;
    private String modelName;

    @Resource
    private ChatModelListener chatModelListener;

    @PostConstruct
    public void init() {
        // 设置 DashScope SDK 全局超时时间
        try {
            Constants.connectionConfigurations = ConnectionConfigurations.builder()
                    .connectTimeout(Duration.ofSeconds(20))
                    .readTimeout(Duration.ofSeconds(300))
                    .writeTimeout(Duration.ofSeconds(300))
                    .build();
        } catch (Exception e) {
            System.err.println("Failed to configure DashScope timeout: " + e.getMessage());
        }
    }

    @Bean
    public ChatModel myQwenChatModel() {
        // 确保在使用前设置超时
        try {
            Constants.connectionConfigurations = ConnectionConfigurations.builder()
                    .connectTimeout(Duration.ofSeconds(20))
                    .readTimeout(Duration.ofSeconds(300))
                    .writeTimeout(Duration.ofSeconds(300))
                    .build();
        } catch (Exception e) {
            System.err.println("Failed to configure DashScope timeout: " + e.getMessage());
        }
        
        return QwenChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .listeners(List.of(chatModelListener))
                .build();
    }
}
