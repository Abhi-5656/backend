package com.wfm.experts.setup.wfm.paypolicy.engine.util;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Utility class for evaluating SpEL (Spring Expression Language) expressions in the rule engine.
 */
public class SpELUtil {

    private static final ExpressionParser PARSER = new SpelExpressionParser();

    private SpELUtil() {
        // Utility class
    }

    /**
     * Evaluate a SpEL expression with the given root object as context.
     *
     * @param expression The SpEL expression (String)
     * @param rootObject The root object for variable/property reference
     * @param <T>        Result type
     * @return Evaluated result of type T
     */
    @SuppressWarnings("unchecked")
    public static <T> T evaluate(String expression, Object rootObject) {
        StandardEvaluationContext context = new StandardEvaluationContext(rootObject);
        Expression expr = PARSER.parseExpression(expression);
        return (T) expr.getValue(context);
    }

    /**
     * Evaluate a SpEL expression with a context that can have extra variables.
     *
     * @param expression   The SpEL expression (String)
     * @param rootObject   The root object for variable/property reference
     * @param variables    Optional additional variables (name, value pairs)
     * @param <T>          Result type
     * @return Evaluated result of type T
     */
    @SuppressWarnings("unchecked")
    public static <T> T evaluate(String expression, Object rootObject, java.util.Map<String, Object> variables) {
        StandardEvaluationContext context = new StandardEvaluationContext(rootObject);
        if (variables != null) {
            variables.forEach(context::setVariable);
        }
        Expression expr = PARSER.parseExpression(expression);
        return (T) expr.getValue(context);
    }
}
