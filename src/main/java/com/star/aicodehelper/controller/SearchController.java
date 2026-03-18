package com.star.aicodehelper.controller;

import com.star.aicodehelper.common.BaseResponse;
import com.star.aicodehelper.common.ResultUtils;
import com.star.aicodehelper.model.entity.User;
import com.star.aicodehelper.model.es.ChatMessageDocument;
import com.star.aicodehelper.service.SearchService;
import com.star.aicodehelper.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/search")
@Tag(name = "搜索模块")
public class SearchController {

    @Resource
    private SearchService searchService;

    @Resource
    private UserService userService;

    @GetMapping("/chat")
    @Operation(summary = "搜索聊天记录", description = "基于 Elasticsearch 的全文检索，支持高亮")
    public BaseResponse<Page<ChatMessageDocument>> searchChatHistory(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new RuntimeException("未登录");
        }

        Page<ChatMessageDocument> result = searchService.searchChatHistory(keyword, loginUser.getId(), page, size);
        return ResultUtils.success(result);
    }

    @PostMapping("/sync")
    @Operation(summary = "全量同步数据", description = "将 MySQL 中的聊天记录全量同步到 Elasticsearch")
    public BaseResponse<Integer> syncFullData() {
        int count = searchService.syncFullData();
        return ResultUtils.success(count);
    }
}
