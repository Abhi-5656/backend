package com.wfm.experts.setup.wfm.paypolicy.dto;

import java.time.LocalDateTime;

public class RuleExecutionLogDTO {
    private String ruleName;
    private String ruleType;
    private String input;
    private String output;
    private boolean success;
    private String errorMessage;
    private LocalDateTime executedAt;
    private long durationMs;

    public RuleExecutionLogDTO() {
    }

    public RuleExecutionLogDTO(String ruleName, String ruleType, String input, String output, boolean success,
                               String errorMessage, LocalDateTime executedAt, long durationMs) {
        this.ruleName = ruleName;
        this.ruleType = ruleType;
        this.input = input;
        this.output = output;
        this.success = success;
        this.errorMessage = errorMessage;
        this.executedAt = executedAt;
        this.durationMs = durationMs;
    }

    // Getters and setters

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    @Override
    public String toString() {
        return "RuleExecutionLogDTO{" +
                "ruleName='" + ruleName + '\'' +
                ", ruleType='" + ruleType + '\'' +
                ", input='" + input + '\'' +
                ", output='" + output + '\'' +
                ", success=" + success +
                ", errorMessage='" + errorMessage + '\'' +
                ", executedAt=" + executedAt +
                ", durationMs=" + durationMs +
                '}';
    }
}
