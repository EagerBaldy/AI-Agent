package com.star.aicodehelper.ai.tools;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class TimeTool {

    @Tool("获取当前的日期和时间。当用户询问今天几号、现在几点等时间相关问题时调用此工具。")
    public String getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return "当前日期和时间是：" + now.format(formatter);
    }
}
