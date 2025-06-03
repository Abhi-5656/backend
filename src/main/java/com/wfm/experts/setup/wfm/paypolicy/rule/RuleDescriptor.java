package com.wfm.experts.setup.wfm.paypolicy.rule;

import java.util.Objects;

/**
 * Descriptor for a pay policy rule. Used for dynamic rule registration and lookup.
 */
public class RuleDescriptor {

    private final String ruleType;          // E.g., "Attendance", "Break", etc.
    private final String ruleName;          // E.g., "AttendanceRuleImpl"
    private final Class<? extends PayPolicyRule> ruleClass;
    private final String description;

    public RuleDescriptor(String ruleType, String ruleName, Class<? extends PayPolicyRule> ruleClass, String description) {
        this.ruleType = ruleType;
        this.ruleName = ruleName;
        this.ruleClass = ruleClass;
        this.description = description;
    }

    public String getRuleType() {
        return ruleType;
    }

    public String getRuleName() {
        return ruleName;
    }

    public Class<? extends PayPolicyRule> getRuleClass() {
        return ruleClass;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RuleDescriptor)) return false;
        RuleDescriptor that = (RuleDescriptor) o;
        return Objects.equals(ruleType, that.ruleType) &&
                Objects.equals(ruleName, that.ruleName) &&
                Objects.equals(ruleClass, that.ruleClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleType, ruleName, ruleClass);
    }

    @Override
    public String toString() {
        return "RuleDescriptor{" +
                "ruleType='" + ruleType + '\'' +
                ", ruleName='" + ruleName + '\'' +
                ", ruleClass=" + ruleClass.getName() +
                ", description='" + description + '\'' +
                '}';
    }
}
