// src/main/java/com/wfm/experts/setup/wfm/leavepolicy/exception/LeaveProfileAlreadyExistsException.java
package com.wfm.experts.setup.wfm.leavepolicy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when attempting to create or rename a LeaveProfile to a name that already exists.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class LeaveProfileAlreadyExistsException extends RuntimeException {

    public LeaveProfileAlreadyExistsException(String profileName) {
        super("A LeaveProfile with name '" + profileName + "' already exists.");
    }
}
