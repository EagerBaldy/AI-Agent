package com.star.aicodehelper.ai.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Configuration
@EnableAsync
@Slf4j
public class RagConfig {

    @Resource
    private EmbeddingModel qwenEmbeddingModel;

    // 为每个助手定义独立的 EmbeddingStore
    @Bean
    public EmbeddingStore<TextSegment> codeEmbeddingStore() { return new InMemoryEmbeddingStore<>(); }
    
    @Bean
    public EmbeddingStore<TextSegment> travelEmbeddingStore() { return new InMemoryEmbeddingStore<>(); }
    
    @Bean
    public EmbeddingStore<TextSegment> essayEmbeddingStore() { return new InMemoryEmbeddingStore<>(); }
    
    @Bean
    public EmbeddingStore<TextSegment> materialEmbeddingStore() { return new InMemoryEmbeddingStore<>(); }
    
    @Bean
    public EmbeddingStore<TextSegment> medicalEmbeddingStore() { return new InMemoryEmbeddingStore<>(); }
    
    @Bean
    public EmbeddingStore<TextSegment> collegeEmbeddingStore() { return new InMemoryEmbeddingStore<>(); }

    // 初始化 RAG 知识库
    // 移除 @PostConstruct，避免自引用或过早初始化导致的问题
    // @PostConstruct
    public void init() {
        // ...
    }

    // 通过 ApplicationRunner 或 CommandLineRunner 在应用启动后执行初始化
    @Bean
    public org.springframework.boot.ApplicationRunner initKnowledgeBaseRunner() {
        return args -> {
            loadKnowledgeBase("code", codeEmbeddingStore());
            loadKnowledgeBase("travel", travelEmbeddingStore());
            loadKnowledgeBase("essay", essayEmbeddingStore());
            loadKnowledgeBase("material", materialEmbeddingStore());
            loadKnowledgeBase("medical", medicalEmbeddingStore());
            loadKnowledgeBase("college", collegeEmbeddingStore());
        };
    }

    @Async
    public CompletableFuture<Void> loadKnowledgeBase(String type, EmbeddingStore<TextSegment> store) {
        try {
            String pathStr = "src/main/resources/docs/knowledge/" + type;
            Path path = Paths.get(pathStr);
            if (!Files.exists(path)) {
                log.warn("Knowledge base directory not found: {}", pathStr);
                return CompletableFuture.completedFuture(null);
            }

            log.info("Start loading knowledge base for: {}", type);
            List<Document> documents = FileSystemDocumentLoader.loadDocuments(pathStr);
            
            if (documents.isEmpty()) {
                log.info("No documents found for: {}", type);
                return CompletableFuture.completedFuture(null);
            }

            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .documentTransformer(new AdvancedMetadataTransformer()) // 使用自定义元数据提取器
                    .documentSplitter(DocumentSplitters.recursive(1000, 200)) // 递归字符分割
                    .textSegmentTransformer(textSegment -> {
                        // 将元数据合并到文本中以增强检索（简单模拟 Rerank 的一部分效果）
                        String title = textSegment.metadata().getString("title");
                        String tags = textSegment.metadata().getString("tags");
                        String context = "";
                        if (title != null) context += "Title: " + title + "\n";
                        if (tags != null) context += "Tags: " + tags + "\n";
                        return TextSegment.from(context + textSegment.text(), textSegment.metadata());
                    })
                    .embeddingModel(qwenEmbeddingModel)
                    .embeddingStore(store)
                    .build();

            ingestor.ingest(documents);
            log.info("Successfully loaded {} documents for {}", documents.size(), type);
            
        } catch (Exception e) {
            log.error("Failed to load knowledge base for {}", type, e);
        }
        return CompletableFuture.completedFuture(null);
    }

    // 定义各助手的 Retriever Bean
    
    @Bean
    public ContentRetriever codeRetriever(EmbeddingStore<TextSegment> codeEmbeddingStore) {
        return createRetriever(codeEmbeddingStore);
    }

    @Bean
    public ContentRetriever travelRetriever(EmbeddingStore<TextSegment> travelEmbeddingStore) {
        return createRetriever(travelEmbeddingStore);
    }

    @Bean
    public ContentRetriever essayRetriever(EmbeddingStore<TextSegment> essayEmbeddingStore) {
        return createRetriever(essayEmbeddingStore);
    }

    @Bean
    public ContentRetriever materialRetriever(EmbeddingStore<TextSegment> materialEmbeddingStore) {
        return createRetriever(materialEmbeddingStore);
    }

    @Bean
    public ContentRetriever medicalRetriever(EmbeddingStore<TextSegment> medicalEmbeddingStore) {
        return createRetriever(medicalEmbeddingStore);
    }

    @Bean
    public ContentRetriever collegeRetriever(EmbeddingStore<TextSegment> collegeEmbeddingStore) {
        return createRetriever(collegeEmbeddingStore);
    }

    private ContentRetriever createRetriever(EmbeddingStore<TextSegment> store) {
        // 使用 Lazy 避免循环依赖
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(qwenEmbeddingModel)
                .maxResults(5) // top-k = 5
                .minScore(0.6) // 适当降低阈值以支持更广泛的匹配
                .build();
    }
}
