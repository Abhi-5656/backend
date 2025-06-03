package com.wfm.experts.setup.wfm.paypolicy.engine.exception;

/**
 * Custom exception for any Pay Policy Rule Engine failures,
 * including invalid rule config, evaluation errors, registry errors, etc.
 */
public class RuleEngineException extends RuntimeException {

    public RuleEngineException(String message) {
        super(message);
    }

    public RuleEngineException(String message, Throwable cause) {
        super(message, cause);
    }

    public RuleEngineException(Throwable cause) {
        super(cause);
    }
}
