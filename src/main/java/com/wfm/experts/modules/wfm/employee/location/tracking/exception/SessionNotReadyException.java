package com.wfm.experts.modules.wfm.employee.location.tracking.exception;

public class SessionNotReadyException extends RuntimeException {
    private final Long sessionId;
    public SessionNotReadyException(Long sessionId) {
        super("Tracking session " + sessionId + " not visible yet.");
        this.sessionId = sessionId;
    }
    public Long getSessionId() { return sessionId; }
}
