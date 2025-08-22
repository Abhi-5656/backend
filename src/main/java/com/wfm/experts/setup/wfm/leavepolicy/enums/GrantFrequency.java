package com.wfm.experts.setup.wfm.leavepolicy.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum GrantFrequency {
    @JsonProperty("One Time")
    ONE_TIME,
    @JsonProperty("Repeatedly")
    REPEATEDLY
}