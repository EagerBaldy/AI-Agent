package com.star.aicodehelper.controller;

import com.star.aicodehelper.agent.core.KoneManus;
import com.star.aicodehelper.agent.core.ReActAgent;
import com.star.aicodehelper.model.entity.AgentMessage;
import com.star.aicodehelper.model.entity.User;
import com.star.aicodehelper.service.AgentMessageService;
import com.star.aicodehelper.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/agent")
@CrossOrigin(origins = {"http://localhost:3001", "http://localhost:5173"}, allowCredentials = "true")
public class AgentController {

    @Resource
    private ReActAgent reActAgent;

    @Resource
    private KoneManus koneManus;

    @Resource
    private AgentMessageService agentMessageService;

    @Resource
    private UserService userService;

    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(
            @RequestParam String message,
            @RequestParam(required = false) Long sessionId,
            HttpServletRequest request) {
        
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser != null ? loginUser.getId() : 0L; // 暂未强制登录，或使用默认ID
        Long finalSessionId = sessionId != null ? sessionId : 0L;

        // 保存用户输入
        saveUserMessage(userId, finalSessionId, message);

        return koneManus.execute(message, userId, finalSessionId)
                .doOnComplete(() -> {
                    // 这里只能捕获到流结束，无法直接获取完整的思考过程和最终答案
                    // 理想情况下，KoneManus.execute 应该返回包含上下文的对象，或者在内部保存
                    // 由于目前架构是返回 Flux<String>，我们暂时只保存用户输入，
                    // Agent 的详细思考过程建议由前端接收完整后回传保存，或者改造 Agent 核心逻辑
                    // 方案二：前端负责保存 Agent 的完整输出（目前前端已实现 saveMessage）
                    // 方案三：Agent 内部保存（需要重构 AgentContext 传递）
                });
    }
    
    // 支持 POST 请求，解决部分代理或前端框架可能使用 POST 的问题
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatPost(@RequestBody String message) {
        // 如果 message 是 JSON 格式 {"message": "..."} 需要解析，这里简单假设直接传文本或 urlencoded
        // 为兼容简单文本
        // POST 暂时不支持 userId/sessionId，或者需要从 request body 解析
        // 这里暂时保持原样，如果需要支持保存，需要扩展
        return koneManus.execute(message);
    }

    @GetMapping("/history")
    public List<AgentMessage> getHistory(
            @RequestParam Long sessionId,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new RuntimeException("未登录");
        }
        
        List<AgentMessage> list = agentMessageService.lambdaQuery()
                .eq(AgentMessage::getUserId, loginUser.getId())
                .eq(AgentMessage::getSessionId, sessionId)
                .orderByAsc(AgentMessage::getCreateTime)
                .list();
        System.out.println("Get history for sessionId: " + sessionId + ", userId: " + loginUser.getId() + ", size: " + list.size());
        return list;
    }

    private void saveUserMessage(Long userId, Long sessionId, String content) {
        if (userId == 0 && sessionId == 0) return; // 只有当两者都为0时才不保存，或者根据业务逻辑
        try {
            AgentMessage agentMessage = new AgentMessage();
            agentMessage.setUserId(userId);
            agentMessage.setSessionId(sessionId);
            agentMessage.setRole("user");
            agentMessage.setContent(content);
            agentMessage.setCreateTime(new Date());
            agentMessageService.save(agentMessage);
        } catch (Exception e) {
            // 忽略保存错误，不影响主流程
            System.err.println("Failed to save agent user message: " + e.getMessage());
        }
    }
}
