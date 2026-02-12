package com.star.aicodehelper.ai.tools;

import dev.langchain4j.agent.tool.Tool;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
public class WebSearchTool {

    @Tool("Search the web for information when the user asks for current events, news, or information not in your knowledge base.")
    public String searchWeb(String query) {
        try {
            String url = "https://www.bing.com/search?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(5000)
                    .get();
            
            Elements results = doc.select("li.b_algo");
            if (results.isEmpty()) {
                return "No search results found for: " + query;
            }

            return results.stream()
                    .limit(5)
                    .map(element -> {
                        String title = element.select("h2").text();
                        String snippet = element.select(".b_caption p").text();
                        if (snippet.isEmpty()) {
                             snippet = element.select(".b_algoSlug").text();
                        }
                        return "Title: " + title + "\nSnippet: " + snippet + "\n";
                    })
                    .collect(Collectors.joining("\n---\n"));
        } catch (Exception e) {
            return "Error performing web search: " + e.getMessage();
        }
    }
}
