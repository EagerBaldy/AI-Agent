package com.star.aicodehelper.agent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentContext {
    private String userInput;
    private String history;
    @Builder.Default
    private List<AgentStep> steps = new ArrayList<>();
    private String finalAnswer;
    private boolean isFinished;
    
    public void addStep(AgentStep step) {
        this.steps.add(step);
    }
}
