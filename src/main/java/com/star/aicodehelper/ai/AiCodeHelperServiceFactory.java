package com.star.aicodehelper.ai;

import com.alibaba.dashscope.assistants.Assistant;
import com.star.aicodehelper.ai.tools.InterviewQuestionTool;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.List;

@Configuration
public class AiCodeHelperServiceFactory {

    @Resource
    private ChatModel myQwenChatModel;

    @Resource
    private StreamingChatModel qwenStreamingChatModel;

    // 移除这里注入的 ContentRetriever，改用 Lazy 注入或者直接在方法参数注入，
    // 但 AiServices.builder 需要实例。
    // 循环依赖原因：RagConfig 依赖 QwenEmbeddingModel (可能在其他配置中)，
    // 而 AiCodeHelperServiceFactory 依赖 RagConfig 的 Bean。
    // 如果 QwenEmbeddingModel 和 ChatModel 在同一个 Config 中，可能导致那个 Config 依赖 AiCodeHelperServiceFactory (不太可能)。
    // 另一种可能是：AiCodeHelperServiceFactory -> RagConfig -> ... -> AiCodeHelperServiceFactory
    
    // 检查 RagConfig 依赖：
    // EmbeddingModel qwenEmbeddingModel; (Resource)
    
    // 检查 AiCodeHelperServiceFactory 依赖：
    // ContentRetriever codeRetriever; (Resource -> RagConfig)
    
    // 看起来没问题，除非 qwenEmbeddingModel 的定义依赖了 AiCodeHelperServiceFactory？
    // 或者其他 Bean。
    
    // 尝试解决：使用 @Lazy 注入 RagConfig 的 Bean
    
    @Resource
    @Lazy
    private ContentRetriever codeRetriever;
    @Resource
    @Lazy
    private ContentRetriever travelRetriever;
    @Resource
    @Lazy
    private ContentRetriever essayRetriever;
    @Resource
    @Lazy
    private ContentRetriever materialRetriever;
    @Resource
    @Lazy
    private ContentRetriever medicalRetriever;
    @Resource
    @Lazy
    private ContentRetriever collegeRetriever;

    @Resource
    private McpToolProvider mcpToolProvider;

    @Resource
    private com.star.aicodehelper.ai.tools.WebSearchTool webSearchTool;

    @Bean
    public AiCodeHelperService aiCodeHelperService() {
        // 会话记忆
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
        // 构造 AI Service
        AiCodeHelperService aiCodeHelperService = AiServices.builder(AiCodeHelperService.class)
                .chatModel(myQwenChatModel)
                .streamingChatModel(qwenStreamingChatModel)
                .chatMemory(chatMemory)
                .chatMemoryProvider(memoryId ->
                        MessageWindowChatMemory.withMaxMessages(10)) // 每个会话独立存储
                .contentRetriever(codeRetriever) // RAG 检索增强生成
                .tools(new InterviewQuestionTool(), webSearchTool) // 工具调用
                .toolProvider(mcpToolProvider) // MCP 工具调用
                .build();
        return aiCodeHelperService;
    }

    @Bean
    public AiTravelHelperService aiTravelHelperService() {
        return AiServices.builder(AiTravelHelperService.class)
                .chatModel(myQwenChatModel)
                .streamingChatModel(qwenStreamingChatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(travelRetriever)
                .tools(webSearchTool)
                .build();
    }

    @Bean
    public AiEssayTeacherService aiEssayTeacherService() {
        return AiServices.builder(AiEssayTeacherService.class)
                .chatModel(myQwenChatModel)
                .streamingChatModel(qwenStreamingChatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(essayRetriever)
                .tools(webSearchTool)
                .build();
    }

    @Bean
    public AiMaterialEncyclopediaService aiMaterialEncyclopediaService() {
        return AiServices.builder(AiMaterialEncyclopediaService.class)
                .chatModel(myQwenChatModel)
                .streamingChatModel(qwenStreamingChatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(materialRetriever)
                .tools(webSearchTool)
                .build();
    }

    @Bean
    public AiMedicalService aiMedicalService() {
        return AiServices.builder(AiMedicalService.class)
                .chatModel(myQwenChatModel)
                .streamingChatModel(qwenStreamingChatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(medicalRetriever)
                .tools(webSearchTool)
                .build();
    }

    @Bean
    public AiCollegePlanningService aiCollegePlanningService() {
        return AiServices.builder(AiCollegePlanningService.class)
                .chatModel(myQwenChatModel)
                .streamingChatModel(qwenStreamingChatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(collegeRetriever)
                .tools(webSearchTool)
                .build();
    }
}
