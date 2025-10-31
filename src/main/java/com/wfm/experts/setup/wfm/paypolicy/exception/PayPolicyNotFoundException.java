package com.wfm.experts.setup.wfm.paypolicy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PayPolicyNotFoundException extends RuntimeException {
    public PayPolicyNotFoundException(String message) {
        super(message);
    }
}