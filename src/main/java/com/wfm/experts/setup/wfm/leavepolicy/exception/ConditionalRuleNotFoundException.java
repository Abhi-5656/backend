package com.wfm.experts.setup.wfm.leavepolicy.exception;// ConditionalRuleNotFoundException.java


/**
 * Thrown when a ConditionalRule with the given id cannot be found.
 */
public class ConditionalRuleNotFoundException extends ResourceNotFoundException {
    public ConditionalRuleNotFoundException(Long id) {
        super("ConditionalRule not found with id: " + id);
    }
}
