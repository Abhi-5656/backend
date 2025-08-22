package com.wfm.experts.setup.wfm.leavepolicy.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Gender {
    @JsonProperty("Any")
    ANY,
    @JsonProperty("Male")
    MALE,
    @JsonProperty("Female")
    FEMALE
}