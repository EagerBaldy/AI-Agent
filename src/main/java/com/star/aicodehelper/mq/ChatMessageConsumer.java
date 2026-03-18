package com.star.aicodehelper.mq;

import com.star.aicodehelper.config.KafkaConfig;
import com.star.aicodehelper.esdao.ChatMessageEsDao;
import com.star.aicodehelper.model.entity.ChatMessage;
import com.star.aicodehelper.model.es.ChatMessageDocument;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ChatMessageConsumer {

    @Resource
    private ChatMessageEsDao chatMessageEsDao;

    @KafkaListener(topics = KafkaConfig.ES_SYNC_TOPIC, groupId = "ai-code-helper-group")
    public void receiveMessage(ChatMessage chatMessage, Acknowledgment acknowledgment) {
        log.info("Received message from MQ: {}", chatMessage.getId());
        
        try {
            // 转换为 ES 文档对象
            ChatMessageDocument doc = new ChatMessageDocument();
            doc.setId(chatMessage.getId());
            doc.setUserId(chatMessage.getUserId());
            doc.setSessionId(chatMessage.getMemoryId().longValue()); // 注意：数据库中是memoryId，ES中是sessionId
            doc.setRole(chatMessage.getRole());
            doc.setContent(chatMessage.getContent());
            doc.setCreateTime(chatMessage.getCreateTime());

            // 写入 ES
            chatMessageEsDao.save(doc);
            
            // 手动确认消息
            acknowledgment.acknowledge();
            log.info("Synced chat message to ES: {}", chatMessage.getId());
        } catch (Exception e) {
            log.error("Failed to sync chat message to ES: {}", chatMessage.getId(), e);
            // 发生异常时，不确认消息，由 Kafka 重新投递或进入死信队列
            // acknowledgment.nack(Duration.ofSeconds(1)); // Spring Kafka 2.8+ 支持
        }
    }
}
