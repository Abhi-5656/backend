package com.wfm.experts.setup.wfm.paypolicy.engine.registry.impl;

import com.wfm.experts.setup.wfm.paypolicy.engine.registry.PayPolicyRuleRegistry;
import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PayPolicyRuleRegistryImpl implements PayPolicyRuleRegistry {

    private final Map<String, PayPolicyRule> ruleMap = new HashMap<>();

    // Constructor injection of all rules implementing PayPolicyRule
    public PayPolicyRuleRegistryImpl(List<PayPolicyRule> rules) {
        for (PayPolicyRule rule : rules) {
            ruleMap.put(rule.getName(), rule);
        }
    }

    @Override
    public List<PayPolicyRule> getAllRules() {
        return List.copyOf(ruleMap.values());
    }

    @Override
    public PayPolicyRule getRuleByName(String name) {
        return ruleMap.get(name);
    }

    @Override
    public List<PayPolicyRule> getRulesForPolicy(Long policyId) {
        // In real life: fetch from DB or config.
        // Here: return all rules.
        return getAllRules();
    }
}
