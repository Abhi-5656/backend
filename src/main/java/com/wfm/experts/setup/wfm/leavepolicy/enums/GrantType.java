package com.wfm.experts.setup.wfm.leavepolicy.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum GrantType {
    @JsonProperty("Fixed")
    FIXED,
    @JsonProperty("Earned")
    EARNED
}