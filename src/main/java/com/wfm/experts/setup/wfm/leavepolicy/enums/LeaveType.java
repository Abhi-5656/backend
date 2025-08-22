package com.wfm.experts.setup.wfm.leavepolicy.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum LeaveType {
    @JsonProperty("Paid")
    PAID,
    @JsonProperty("Unpaid")
    UNPAID
}