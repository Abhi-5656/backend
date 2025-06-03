package com.wfm.experts.setup.wfm.paypolicy.engine.evaluator;

import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;

public interface PayPolicyRuleEvaluator {
    boolean evaluate(PayPolicyRule rule, PayPolicyExecutionContext context);
}
