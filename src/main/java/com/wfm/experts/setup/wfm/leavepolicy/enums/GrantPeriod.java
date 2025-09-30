// Save as: src/main/java/com/wfm/experts/setup/wfm/leavepolicy/enums/GrantPeriod.java
package com.wfm.experts.setup.wfm.leavepolicy.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum GrantPeriod {
    @JsonProperty("Monthly")
    MONTHLY,
    @JsonProperty("Yearly")
    YEARLY,
    @JsonProperty("Pay Period")
    PAY_PERIOD
}