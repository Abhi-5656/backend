package com.wfm.experts.setup.wfm.paypolicy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PayPolicyAlreadyExistsException extends RuntimeException {
    public PayPolicyAlreadyExistsException(String message) {
        super(message);
    }
}