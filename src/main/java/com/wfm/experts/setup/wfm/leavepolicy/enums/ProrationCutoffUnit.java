package com.wfm.experts.setup.wfm.leavepolicy.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ProrationCutoffUnit {
    @JsonProperty("DayOfMonth")
    DAY_OF_MONTH,
    @JsonProperty("DayOfYear")
    DAY_OF_YEAR,
    @JsonProperty("DayOfPayPeriod")
    DAY_OF_PAY_PERIOD
}