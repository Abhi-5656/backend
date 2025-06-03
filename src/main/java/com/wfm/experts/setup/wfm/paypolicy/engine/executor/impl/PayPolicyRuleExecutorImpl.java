package com.wfm.experts.setup.wfm.paypolicy.engine.executor.impl;

import com.wfm.experts.setup.wfm.paypolicy.engine.executor.PayPolicyRuleExecutor;
import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PayPolicyRuleExecutorImpl implements PayPolicyRuleExecutor {

    @Override
    public List<PayPolicyRuleResultDTO> executeRules(List<PayPolicyRule> rules, PayPolicyExecutionContext context) {
        List<PayPolicyRuleResultDTO> results = new ArrayList<>();
        for (PayPolicyRule rule : rules) {
            if (rule.evaluate(context)) {
                PayPolicyRuleResultDTO result = rule.execute(context);
                results.add(result);
            }
        }
        return results;
    }

    @Override
    public PayPolicyRuleResultDTO executeRule(PayPolicyRule rule, PayPolicyExecutionContext context) {
        if (rule.evaluate(context)) {
            return rule.execute(context);
        }
        return null;
    }
}
