package com.star.aicodehelper.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String ES_SYNC_TOPIC = "es_sync_topic";

    @Bean
    public NewTopic esSyncTopic() {
        return TopicBuilder.name(ES_SYNC_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
