package com.wfm.experts.setup.wfm.leavepolicy.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum GrantExpiration {
    @JsonProperty("Yearly")
    YEARLY,
    @JsonProperty("Pay Period")
    PAY_PERIOD,
    @JsonProperty("No Expiration")
    NO_EXPIRATION
}