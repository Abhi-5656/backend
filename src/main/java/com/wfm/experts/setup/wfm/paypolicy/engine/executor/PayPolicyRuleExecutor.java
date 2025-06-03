package com.wfm.experts.setup.wfm.paypolicy.engine.executor;

import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;

import java.util.List;

public interface PayPolicyRuleExecutor {
    List<PayPolicyRuleResultDTO> executeRules(List<PayPolicyRule> rules, PayPolicyExecutionContext context);
    PayPolicyRuleResultDTO executeRule(PayPolicyRule rule, PayPolicyExecutionContext context);
}
