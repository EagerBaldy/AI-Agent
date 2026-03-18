package com.star.aicodehelper.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.star.aicodehelper.esdao.ChatMessageEsDao;
import com.star.aicodehelper.mapper.ChatMessageMapper;
import com.star.aicodehelper.model.entity.ChatMessage;
import com.star.aicodehelper.model.es.ChatMessageDocument;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightFieldParameters;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SearchService {

    @Resource
    private ElasticsearchOperations elasticsearchOperations;

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private ChatMessageEsDao chatMessageEsDao;

    public Page<ChatMessageDocument> searchChatHistory(String keyword, Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // 构建查询条件
        // 注意：userId 是 Long 类型，不应该参与全文检索，应该作为 filter 条件
        // content 是 Text 类型，应该使用 matches 或 contains 进行全文检索
        Criteria criteria = new Criteria("userId").is(userId)
                .and(new Criteria("content").matches(keyword));

        CriteriaQuery query = new CriteriaQuery(criteria);
        query.setPageable(pageable);

        // 设置高亮
        HighlightFieldParameters highlightParameters = HighlightFieldParameters.builder()
                .withPreTags("<span style='color: red'>")
                .withPostTags("</span>")
                .build();
        
        HighlightField highlightField = new HighlightField("content", highlightParameters);
        
        query.setHighlightQuery(new HighlightQuery(new Highlight(Collections.singletonList(highlightField)), ChatMessageDocument.class));

        SearchHits<ChatMessageDocument> searchHits = elasticsearchOperations.search(query, ChatMessageDocument.class);

        // 处理高亮结果
        List<ChatMessageDocument> list = searchHits.getSearchHits().stream().map(hit -> {
            ChatMessageDocument doc = hit.getContent();
            List<String> highlightContent = hit.getHighlightField("content");
            if (highlightContent != null && !highlightContent.isEmpty()) {
                doc.setContent(highlightContent.get(0)); // 替换为高亮内容
            }
            return doc;
        }).collect(Collectors.toList());

        return new PageImpl<>(list, pageable, searchHits.getTotalHits());
    }

    /**
     * 全量同步 MySQL 数据到 ES
     *
     * @return 同步条数
     */
    public int syncFullData() {
        // 1. 查询 MySQL 所有数据（实际生产中应分页查询，避免 OOM）
        List<ChatMessage> chatMessages = chatMessageMapper.selectList(new QueryWrapper<>());
        if (chatMessages.isEmpty()) {
            return 0;
        }

        // 2. 转换为 ES 文档
        List<ChatMessageDocument> documents = chatMessages.stream().map(msg -> {
            ChatMessageDocument doc = new ChatMessageDocument();
            doc.setId(msg.getId());
            doc.setUserId(msg.getUserId());
            doc.setSessionId(msg.getMemoryId());
            doc.setRole(msg.getRole());
            doc.setContent(msg.getContent());
            doc.setCreateTime(msg.getCreateTime());
            return doc;
        }).collect(Collectors.toList());

        // 3. 批量写入 ES
        chatMessageEsDao.saveAll(documents);
        
        log.info("Full sync completed. Count: {}", documents.size());
        return documents.size();
    }
}
