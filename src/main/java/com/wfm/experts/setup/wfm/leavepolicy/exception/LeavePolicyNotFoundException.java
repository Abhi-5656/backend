// LeavePolicyNotFoundException.java
package com.wfm.experts.setup.wfm.leavepolicy.exception;

/**
 * Thrown when a LeavePolicy with the given id or code cannot be found.
 */
public class LeavePolicyNotFoundException extends ResourceNotFoundException {
    public LeavePolicyNotFoundException(Long id) {
        super("LeavePolicy not found with id: " + id);
    }

    public LeavePolicyNotFoundException(String code, boolean byCode) {
        super("LeavePolicy not found with code: " + code);
    }
}
