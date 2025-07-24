// src/main/java/com/wfm/experts/setup/wfm/leavepolicy/exception/LeaveProfileNotFoundException.java
package com.wfm.experts.setup.wfm.leavepolicy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a LeaveProfile with the given id does not exist.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class LeaveProfileNotFoundException extends RuntimeException {

    public LeaveProfileNotFoundException(Long id) {
        super("LeaveProfile not found with id: " + id);
    }
}
