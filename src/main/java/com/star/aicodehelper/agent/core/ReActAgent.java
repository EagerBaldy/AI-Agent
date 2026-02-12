package com.star.aicodehelper.agent.core;

import com.star.aicodehelper.agent.model.AgentContext;
import com.star.aicodehelper.agent.model.AgentStep;
import com.star.aicodehelper.model.entity.AgentMessage;
import com.star.aicodehelper.service.AgentMessageService;
import dev.langchain4j.model.chat.ChatModel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public abstract class ReActAgent extends BaseAgent {

    private static final int MAX_STEPS = 10;
    
    @Setter
    protected AgentMessageService agentMessageService;

    // 简单的 ReAct 正则匹配
    private static final Pattern ACTION_PATTERN = Pattern.compile("Action:\\s*(.*?)\\nAction Input:\\s*(.*)", Pattern.DOTALL);
    private static final Pattern FINAL_ANSWER_PATTERN = Pattern.compile("Final Answer:\\s*(.*)", Pattern.DOTALL);

    public ReActAgent(ChatModel chatModel) {
        super(chatModel);
    }

    protected abstract String getSystemPrompt();
    
    // 由子类实现工具执行逻辑
    protected abstract String executeTool(String toolName, String toolArgs);

    @Override
    public Flux<String> execute(String input) {
        return execute(input, 0L, 0L);
    }

    @Override
    public Flux<String> execute(String input, Long userId, Long sessionId) {
        return Flux.create(emitter -> {
            AgentContext context = initContext(input);
            // 加载历史记录，恢复上下文
            String history = getHistory(userId, sessionId);
            context.setHistory(history);
            
            int stepCount = 0;

            try {
                emitter.next("🚀 开始执行任务: " + input + "\n\n");
                // 默认展开思考过程
                emitter.next("<details open><summary>观察思考过程</summary>\n\n");

                while (!context.isFinished() && stepCount < MAX_STEPS) {
                    stepCount++;
                    String prompt = buildPrompt(context);
                    
                    String response = chatModel.chat(prompt);
                    log.info("Agent Step {}: Response: {}", stepCount, response);

                    AgentStep step = parseResponse(response);
                    
                    if (step.getAction() != null && !step.getAction().equals("None")) {
                        emitter.next("#### 第 " + stepCount + " 步\n");
                        emitter.next("> **思考**: " + step.getThought() + "\n\n");
                        emitter.next("> 🛠️ **调用工具**: `" + step.getAction() + "` (" + step.getActionInput() + ")\n\n");
                        
                        String observation = executeTool(step.getAction(), step.getActionInput());
                        step.setObservation(observation);
                        context.addStep(step);
                        
                        emitter.next("> 👀 **观察结果**: " + observation + "\n\n");
                        
                        // 保存中间步骤
                        saveAgentStep(userId, sessionId, step);
                    } else {
                        emitter.next("\n</details>\n\n");
                        String finalAnswer = extractFinalAnswer(response);
                        context.setFinalAnswer(finalAnswer);
                        context.setFinished(true);
                        
                        // 如果有最终思考，也输出出来（可选，放在details里或者外面）
                        // 这里我们选择不重复输出思考，直接输出答案
                        // 如果需要输出最终思考：emitter.next("> 🤔 **最终思考**: " + step.getThought() + "\n\n");
                        
                        emitter.next(finalAnswer); // 直接输出答案，不再加前缀
                        
                        // 保存最终答案
                        saveFinalAnswer(userId, sessionId, finalAnswer, step.getThought());
                    }
                }
                
                if (!context.isFinished()) {
                    emitter.next("\n</details>\n\n");
                    emitter.next("⚠️ 任务执行步数超限，强制结束。\n");
                }
                
                emitter.complete();
            } catch (Exception e) {
                log.error("Agent execution failed", e);
                emitter.error(e);
            }
        });
    }

    private void saveAgentStep(Long userId, Long sessionId, AgentStep step) {
        if (agentMessageService == null || userId == null || sessionId == null) return;
        try {
            AgentMessage message = new AgentMessage();
            message.setUserId(userId);
            message.setSessionId(sessionId);
            message.setRole("agent");
            message.setThought(step.getThought());
            message.setAction(step.getAction());
            message.setActionInput(step.getActionInput());
            message.setObservation(step.getObservation());
            message.setCreateTime(new Date());
            agentMessageService.save(message);
        } catch (Exception e) {
            log.error("Failed to save agent step", e);
        }
    }

    private void saveFinalAnswer(Long userId, Long sessionId, String finalAnswer, String thought) {
        if (agentMessageService == null || userId == null || sessionId == null) return;
        try {
            AgentMessage message = new AgentMessage();
            message.setUserId(userId);
            message.setSessionId(sessionId);
            message.setRole("agent");
            message.setContent(finalAnswer);
            message.setThought(thought); // 最后的思考
            message.setCreateTime(new Date());
            agentMessageService.save(message);
        } catch (Exception e) {
            log.error("Failed to save final answer", e);
        }
    }

    private String buildPrompt(AgentContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append(getSystemPrompt()).append("\n\n");
        
        // 添加历史上下文
        if (context.getHistory() != null && !context.getHistory().isEmpty()) {
            sb.append("Previous Conversation History:\n")
              .append(context.getHistory())
              .append("\n");
        }
        
        sb.append("User Question: ").append(context.getUserInput()).append("\n\n");
        
        for (AgentStep step : context.getSteps()) {
            sb.append("Thought: ").append(step.getThought()).append("\n");
            sb.append("Action: ").append(step.getAction()).append("\n");
            sb.append("Action Input: ").append(step.getActionInput()).append("\n");
            sb.append("Observation: ").append(step.getObservation()).append("\n\n");
        }
        
        sb.append("Thought:"); // 引导 LLM 开始思考
        return sb.toString();
    }

    private String getHistory(Long userId, Long sessionId) {
        if (agentMessageService == null || userId == null || sessionId == null) return "";
        try {
            // 获取最近的10条消息作为上下文
            java.util.List<AgentMessage> messages = agentMessageService.lambdaQuery()
                    .eq(AgentMessage::getUserId, userId)
                    .eq(AgentMessage::getSessionId, sessionId)
                    .orderByDesc(AgentMessage::getCreateTime)
                    .last("LIMIT 10")
                    .list();
            
            if (messages == null || messages.isEmpty()) {
                return "";
            }
            
            // 按时间正序排列
            java.util.Collections.reverse(messages);
            
            StringBuilder sb = new StringBuilder();
            for (AgentMessage msg : messages) {
                if ("user".equals(msg.getRole())) {
                    sb.append("User: ").append(msg.getContent()).append("\n");
                } else if ("agent".equals(msg.getRole()) && msg.getContent() != null) {
                    // 只包含最终答案，跳过中间思考步骤（中间步骤content通常为空或只有thought）
                    sb.append("Assistant: ").append(msg.getContent()).append("\n");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("Failed to load history for context", e);
            return "";
        }
    }

    private AgentStep parseResponse(String response) {
        AgentStep step = new AgentStep();
        // 提取 Thought
        int actionIndex = response.indexOf("Action:");
        if (actionIndex != -1) {
            step.setThought(response.substring(0, actionIndex).trim());
            String remaining = response.substring(actionIndex);
            Matcher matcher = ACTION_PATTERN.matcher(remaining);
            if (matcher.find()) {
                step.setAction(matcher.group(1).trim());
                step.setActionInput(matcher.group(2).trim());
            }
        } else {
            // 没有 Action，可能是 Final Answer
            int finalIndex = response.indexOf("Final Answer:");
            if (finalIndex != -1) {
                step.setThought(response.substring(0, finalIndex).trim());
            } else {
                step.setThought(response);
            }
            step.setAction("None");
        }
        return step;
    }
    
    private String extractFinalAnswer(String response) {
        int index = response.indexOf("Final Answer:");
        if (index != -1) {
            return response.substring(index + "Final Answer:".length()).trim();
        }
        return response; // Fallback
    }
}
