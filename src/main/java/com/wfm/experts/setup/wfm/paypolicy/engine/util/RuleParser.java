package com.wfm.experts.setup.wfm.paypolicy.engine.util;

public interface RuleParser<T> {
    T parse(String ruleDefinition, Class<T> ruleType);
}
