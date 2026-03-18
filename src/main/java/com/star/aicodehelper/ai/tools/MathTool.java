package com.star.aicodehelper.ai.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

@Component
public class MathTool {

    private final ExpressionParser parser = new SpelExpressionParser();

    @Tool("执行数学表达式计算。当用户要求进行数学运算（如加减乘除、括号等复杂数学表达式）时调用此工具。注意：表达式字符串必须合法，例如 '3 * (4 + 5)'")
    public String calculateMathExpression(
            @P("数学表达式字符串，如 '1 + 2 * 3'") String expression) {
        try {
            Expression exp = parser.parseExpression(expression);
            Object result = exp.getValue();
            return "计算结果为：" + result;
        } catch (Exception e) {
            return "计算失败，表达式有误：" + e.getMessage();
        }
    }
}
