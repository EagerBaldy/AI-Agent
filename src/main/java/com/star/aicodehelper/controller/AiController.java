package com.star.aicodehelper.controller;

import com.star.aicodehelper.ai.AiCodeHelperService;
import com.star.aicodehelper.ai.AiTravelHelperService;
import com.star.aicodehelper.model.entity.User;
import com.star.aicodehelper.service.ChatHistoryService;
import com.star.aicodehelper.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private AiCodeHelperService aiCodeHelperService;

    @Resource
    private AiTravelHelperService aiTravelHelperService;

    @Resource
    private com.star.aicodehelper.ai.AiEssayTeacherService aiEssayTeacherService;

    @Resource
    private com.star.aicodehelper.ai.AiMaterialEncyclopediaService aiMaterialEncyclopediaService;

    @Resource
    private com.star.aicodehelper.ai.AiMedicalService aiMedicalService;

    @Resource
    private com.star.aicodehelper.ai.AiCollegePlanningService aiCollegePlanningService;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UserService userService;

    @GetMapping("/chat")
    public Flux<ServerSentEvent<String>> chat(long memoryId, String message, HttpServletRequest request) {
        return handleChat(memoryId, message, "code", aiCodeHelperService.chatStream((int) memoryId, message), request);
    }

    @GetMapping("/travel/chat")
    public Flux<ServerSentEvent<String>> travelChat(long memoryId, String message, HttpServletRequest request) {
        return handleChat(memoryId, message, "travel", aiTravelHelperService.chatStream((int) memoryId, message), request);
    }

    @GetMapping("/essay/chat")
    public Flux<ServerSentEvent<String>> essayChat(long memoryId, String message, HttpServletRequest request) {
        return handleChat(memoryId, message, "essay", aiEssayTeacherService.chatStream((int) memoryId, message), request);
    }

    @GetMapping("/material/chat")
    public Flux<ServerSentEvent<String>> materialChat(long memoryId, String message, HttpServletRequest request) {
        return handleChat(memoryId, message, "material", aiMaterialEncyclopediaService.chatStream((int) memoryId, message), request);
    }

    @GetMapping("/medical/chat")
    public Flux<ServerSentEvent<String>> medicalChat(long memoryId, String message, HttpServletRequest request) {
        return handleChat(memoryId, message, "medical", aiMedicalService.chatStream((int) memoryId, message), request);
    }

    @GetMapping("/college/chat")
    public Flux<ServerSentEvent<String>> collegeChat(long memoryId, String message, HttpServletRequest request) {
        return handleChat(memoryId, message, "college", aiCollegePlanningService.chatStream((int) memoryId, message), request);
    }

    private Flux<ServerSentEvent<String>> handleChat(long memoryId, String message, String assistantType, Flux<String> stream, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            return Flux.error(new RuntimeException("未登录"));
        }

        // 保存用户消息
        chatHistoryService.saveMessage(loginUser.getId(), memoryId, assistantType, "user", message);

        // 收集 AI 回复并保存
        StringBuilder aiResponse = new StringBuilder();
        return stream
                .doOnNext(aiResponse::append)
                .doOnComplete(() -> {
                    chatHistoryService.saveMessage(loginUser.getId(), memoryId, assistantType, "ai", aiResponse.toString());
                })
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }
}
