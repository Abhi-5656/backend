package com.wfm.experts.setup.wfm.paypolicy.engine.registry;

import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;

import java.util.List;

public interface PayPolicyRuleRegistry {

    /**
     * @return All registered rules.
     */
    List<PayPolicyRule> getAllRules();

    /**
     * Get rule by unique name.
     * @param name Rule name
     * @return The rule, or null if not found
     */
    PayPolicyRule getRuleByName(String name);

    /**
     * Get rules for a specific policy, if you want tenant/policy specific rule set.
     * (You can wire this to DB, config, or just return all rules for simple use.)
     */
    List<PayPolicyRule> getRulesForPolicy(Long policyId);
}
