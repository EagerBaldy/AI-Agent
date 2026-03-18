package com.star.aicodehelper.esdao;

import com.star.aicodehelper.model.es.ChatMessageDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageEsDao extends ElasticsearchRepository<ChatMessageDocument, Long> {
    List<ChatMessageDocument> findByUserId(Long userId);
}
