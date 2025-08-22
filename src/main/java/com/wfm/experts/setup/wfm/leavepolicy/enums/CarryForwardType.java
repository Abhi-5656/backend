package com.wfm.experts.setup.wfm.leavepolicy.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum CarryForwardType {
    @JsonProperty("days")
    DAYS,
    @JsonProperty("percent")
    PERCENT
}