package com.wfm.experts.setup.wfm.leavepolicy.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Applicability {
    @JsonProperty("Half Day")
    HALF_DAY,
    @JsonProperty("Full Day")
    FULL_DAY
}