package com.star.aicodehelper.agent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentStep {
    private String thought;
    private String action;
    private String actionInput;
    private String observation;
}
