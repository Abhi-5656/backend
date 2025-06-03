package com.wfm.experts.setup.wfm.paypolicy.rule;

import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;

public interface PayPolicyRule {
    String getName();
    boolean evaluate(PayPolicyExecutionContext context);
    PayPolicyRuleResultDTO execute(PayPolicyExecutionContext context);
}
