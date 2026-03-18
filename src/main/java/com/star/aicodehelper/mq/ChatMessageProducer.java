package com.star.aicodehelper.mq;

import com.star.aicodehelper.config.KafkaConfig;
import com.star.aicodehelper.model.entity.ChatMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ChatMessageProducer {

    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 发送消息到 ES 同步队列
     *
     * @param chatMessage 聊天记录
     */
    public void sendSyncMessage(ChatMessage chatMessage) {
        kafkaTemplate.send(KafkaConfig.ES_SYNC_TOPIC, chatMessage);
        log.info("Sent chat message to MQ: {}", chatMessage.getId());
    }
}
