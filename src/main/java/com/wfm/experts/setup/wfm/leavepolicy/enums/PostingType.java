// Update: src/main/java/com/wfm/experts/setup/wfm/leavepolicy/enums/PostingType.java
package com.wfm.experts.setup.wfm.leavepolicy.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum PostingType {
    @JsonProperty("None")
    NONE,
    @JsonProperty("First Day of Month")
    FIRST_DAY_OF_MONTH,
    @JsonProperty("Last Day of Month")
    LAST_DAY_OF_MONTH,
    @JsonProperty("First Day of Year")
    FIRST_DAY_OF_YEAR,
    @JsonProperty("Last Day of Year")
    LAST_DAY_OF_YEAR,
    @JsonProperty("End of Pay Period")
    END_OF_PAY_PERIOD
}