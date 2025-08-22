package com.wfm.experts.setup.wfm.leavepolicy.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum CalculationDateType {
    @JsonProperty("Hire Date")
    HIRE_DATE,
    @JsonProperty("Custom Date")
    CUSTOM_DATE
}