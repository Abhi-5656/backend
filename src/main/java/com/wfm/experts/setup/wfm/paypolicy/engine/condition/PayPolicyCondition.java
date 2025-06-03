package com.wfm.experts.setup.wfm.paypolicy.engine.condition;

import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;

/**
 * Functional interface for evaluating conditions within the pay policy rule engine.
 * Can be implemented using lambdas, custom classes, or even expressions (e.g., SpEL).
 */
@FunctionalInterface
public interface PayPolicyCondition {
    /**
     * Evaluates the condition using the provided execution context.
     * @param context The rule execution context (contains all facts).
     * @return true if the condition is met, false otherwise.
     */
    boolean matches(PayPolicyExecutionContext context);
}
