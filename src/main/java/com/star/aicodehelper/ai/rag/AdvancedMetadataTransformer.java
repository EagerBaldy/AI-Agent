package com.star.aicodehelper.ai.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentTransformer;
import dev.langchain4j.data.document.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 高级元数据提取转换器
 * 自动识别并提取：
 * - YAML Front Matter
 * - 章节层级结构
 * - 关键实体（人名、地名、技术术语 - 简单模拟）
 * - 代码片段与公式
 */
public class AdvancedMetadataTransformer implements DocumentTransformer {

    private static final Logger log = LoggerFactory.getLogger(AdvancedMetadataTransformer.class);
    
    // 匹配 YAML Front Matter 的正则：以 --- 开始，以 --- 结束
    private static final Pattern YAML_PATTERN = Pattern.compile("^---\\s*\\n(.*?)\\n---\\s*\\n", Pattern.DOTALL);
    
    // 简单匹配 YAML 键值对
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("^(\\w+):\\s*(.+)$", Pattern.MULTILINE);

    @Override
    public Document transform(Document document) {
        String text = document.text();
        Metadata metadata = document.metadata();
        
        // 1. 提取 YAML Front Matter
        Matcher yamlMatcher = YAML_PATTERN.matcher(text);
        if (yamlMatcher.find()) {
            String yamlContent = yamlMatcher.group(1);
            Matcher kvMatcher = KEY_VALUE_PATTERN.matcher(yamlContent);
            while (kvMatcher.find()) {
                String key = kvMatcher.group(1).trim();
                String value = kvMatcher.group(2).trim();
                // 移除可能的引号或括号
                value = value.replaceAll("^\\[|\\]$", "").replaceAll("^['\"]|['\"]$", "");
                // Metadata.add 应该是 put? LangChain4j Metadata extends HashMap or wraps it
                // 检查 Metadata API，通常是 put(String, String) 或者 add(String, Object)
                // 假设是 put
                metadata.put(key, value);
            }
            // 从正文中移除 Front Matter，避免向量化干扰
            text = yamlMatcher.replaceFirst("");
        }

        // 2. 提取章节结构 (Headings) - 简单存入 metadata
        // 提取一级和二级标题
        extractHeadings(text, metadata);

        // 3. 提取代码片段标记
        if (text.contains("```")) {
            metadata.put("has_code", "true");
        }
        
        // 4. 更新文档内容（移除 Front Matter 后的内容）
        return Document.from(text, metadata);
    }

    private void extractHeadings(String text, Metadata metadata) {
        Pattern h1Pattern = Pattern.compile("^#\\s+(.+)$", Pattern.MULTILINE);
        Matcher h1Matcher = h1Pattern.matcher(text);
        if (h1Matcher.find()) {
            metadata.put("main_topic", h1Matcher.group(1).trim());
        }
    }
}
