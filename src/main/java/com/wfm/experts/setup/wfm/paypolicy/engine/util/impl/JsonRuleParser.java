package com.wfm.experts.setup.wfm.paypolicy.engine.util.impl;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.wfm.experts.setup.wfm.paypolicy.engine.exception.RuleEngineException;
import com.wfm.experts.setup.wfm.paypolicy.engine.util.RuleParser;

public class JsonRuleParser<T> implements RuleParser<T> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public T parse(String ruleDefinition, Class<T> ruleType) {
        try {
            return objectMapper.readValue(ruleDefinition, ruleType);
        } catch (Exception e) {
            throw new RuleEngineException("Failed to parse rule: " + e.getMessage(), e);
        }
    }
}
