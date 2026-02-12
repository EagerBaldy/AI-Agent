package com.star.aicodehelper.service;

import com.star.aicodehelper.agent.core.KoneManus;
import com.star.aicodehelper.model.entity.AgentMessage;
import dev.langchain4j.model.chat.ChatModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@SpringBootTest
@Transactional
public class AgentSessionRecoveryTest {

    @Autowired
    private AgentMessageService agentMessageService;

    @Autowired
    private KoneManus koneManus;

    @Test
    public void testSessionRecovery() {
        // 1. 模拟保存会话数据
        Long userId = 999L;
        Long sessionId = 888L;

        // 保存中间思考步骤
        AgentMessage step1 = new AgentMessage();
        step1.setUserId(userId);
        step1.setSessionId(sessionId);
        step1.setRole("agent");
        step1.setThought("Thinking about the user request...");
        step1.setAction("Search");
        step1.setActionInput("query");
        step1.setCreateTime(new Date());
        agentMessageService.save(step1);

        // 保存最终回答
        AgentMessage finalAnswer = new AgentMessage();
        finalAnswer.setUserId(userId);
        finalAnswer.setSessionId(sessionId);
        finalAnswer.setRole("agent");
        finalAnswer.setContent("Here is the answer.");
        finalAnswer.setThought("Final thought.");
        finalAnswer.setCreateTime(new Date());
        agentMessageService.save(finalAnswer);

        // 2. 模拟恢复会话（查询历史记录）
        List<AgentMessage> history = agentMessageService.lambdaQuery()
                .eq(AgentMessage::getUserId, userId)
                .eq(AgentMessage::getSessionId, sessionId)
                .orderByAsc(AgentMessage::getCreateTime)
                .list();

        // 3. 验证数据完整性
        Assertions.assertEquals(2, history.size(), "Should retrieve 2 messages");
        
        AgentMessage retrievedStep1 = history.get(0);
        Assertions.assertEquals("Thinking about the user request...", retrievedStep1.getThought());
        Assertions.assertEquals("Search", retrievedStep1.getAction());
        Assertions.assertNull(retrievedStep1.getContent(), "Step 1 content should be null");

        AgentMessage retrievedFinal = history.get(1);
        Assertions.assertEquals("Here is the answer.", retrievedFinal.getContent());
        Assertions.assertEquals("Final thought.", retrievedFinal.getThought());
        
        System.out.println("Session recovery test passed!");
    }

    @Test
    public void testAgentContextRecovery() {
        Long userId = 1000L;
        Long sessionId = 2000L;

        // 1. 准备历史数据
        AgentMessage msg1 = new AgentMessage();
        msg1.setUserId(userId);
        msg1.setSessionId(sessionId);
        msg1.setRole("user");
        msg1.setContent("What is Java?");
        msg1.setCreateTime(new Date(System.currentTimeMillis() - 10000));
        agentMessageService.save(msg1);

        AgentMessage msg2 = new AgentMessage();
        msg2.setUserId(userId);
        msg2.setSessionId(sessionId);
        msg2.setRole("agent");
        msg2.setContent("Java is a programming language.");
        msg2.setCreateTime(new Date(System.currentTimeMillis() - 5000));
        agentMessageService.save(msg2);

        // 2. Mock ChatModel
        ChatModel mockChatModel = Mockito.mock(ChatModel.class);
        Mockito.when(mockChatModel.chat(Mockito.anyString())).thenReturn("Final Answer: It is object-oriented.");

        // 注入 Mock 对象
        // 注意：这里修改了 Spring Bean 的内部状态，可能会影响其他测试，但因为是 Transactional 且是最后一个测试，影响有限
        // 更严谨的做法是在测试后还原，或者使用 @MockBean
        Object originalChatModel = ReflectionTestUtils.getField(koneManus, "chatModel");
        ReflectionTestUtils.setField(koneManus, "chatModel", mockChatModel);

        try {
            // 3. 执行 Agent，触发上下文恢复
            koneManus.execute("Tell me more.", userId, sessionId).blockLast();

            // 4. 验证 Prompt 是否包含历史记录
            ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
            Mockito.verify(mockChatModel, Mockito.atLeastOnce()).chat(promptCaptor.capture());

            String prompt = promptCaptor.getValue();
            System.out.println("Captured Prompt: \n" + prompt);

            Assertions.assertTrue(prompt.contains("Previous Conversation History:"), "Prompt should contain history section");
            Assertions.assertTrue(prompt.contains("User: What is Java?"), "Prompt should contain previous user question");
            Assertions.assertTrue(prompt.contains("Assistant: Java is a programming language."), "Prompt should contain previous assistant answer");
            Assertions.assertTrue(prompt.contains("User Question: Tell me more."), "Prompt should contain current user question");

        } finally {
            // 还原 ChatModel
            ReflectionTestUtils.setField(koneManus, "chatModel", originalChatModel);
        }
    }
}
