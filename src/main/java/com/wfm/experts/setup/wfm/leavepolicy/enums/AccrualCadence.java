package com.wfm.experts.setup.wfm.leavepolicy.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum AccrualCadence {
    @JsonProperty("Monthly")
    MONTHLY,
    @JsonProperty("Pay Period")
    PAY_PERIOD
}