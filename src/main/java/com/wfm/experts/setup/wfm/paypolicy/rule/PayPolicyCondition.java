package com.wfm.experts.setup.wfm.paypolicy.rule;

import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;

@FunctionalInterface
public interface PayPolicyCondition {
    boolean matches(PayPolicyExecutionContext context);
}
