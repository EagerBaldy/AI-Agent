package com.star.aicodehelper.agent.core;

import dev.langchain4j.model.chat.ChatModel;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public abstract class ToolCallAgent extends ReActAgent {

    protected final Map<String, Function<String, String>> tools = new HashMap<>();
    protected final Map<String, String> toolDescriptions = new HashMap<>();

    public ToolCallAgent(ChatModel chatModel) {
        super(chatModel);
        registerTools();
    }

    protected abstract void registerTools();

    protected void addTool(String name, String description, Function<String, String> executor) {
        tools.put(name, executor);
        toolDescriptions.put(name, description);
    }

    @Override
    protected String executeTool(String toolName, String toolArgs) {
        Function<String, String> tool = tools.get(toolName);
        if (tool == null) {
            return "Error: Tool '" + toolName + "' not found. Available tools: " + tools.keySet();
        }
        try {
            return tool.apply(toolArgs);
        } catch (Exception e) {
            log.error("Tool execution error", e);
            return "Error executing tool: " + e.getMessage();
        }
    }

    @Override
    protected String getSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a smart AI assistant. Answer the following questions as best you can. You have access to the following tools:\n\n");
        
        toolDescriptions.forEach((name, desc) -> 
            sb.append(name).append(": ").append(desc).append("\n")
        );
        
        sb.append("\nUse the following format:\n\n");
        sb.append("Question: the input question you must answer\n");
        sb.append("Thought: you should always think about what to do\n");
        sb.append("Action: the action to take, should be one of [");
        sb.append(String.join(", ", tools.keySet()));
        sb.append("]\n");
        sb.append("Action Input: the input to the action\n");
        sb.append("Observation: the result of the action\n");
        sb.append("... (this Thought/Action/Action Input/Observation can repeat N times)\n");
        sb.append("Thought: I now know the final answer\n");
        sb.append("Final Answer: the final answer to the original input question\n");
        
        return sb.toString();
    }
}
