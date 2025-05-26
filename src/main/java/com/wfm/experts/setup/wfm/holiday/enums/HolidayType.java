package com.wfm.experts.setup.wfm.holiday.enums;

import lombok.Getter;

@Getter
public enum HolidayType {
    NATIONAL("National Holiday"),
    RELIGIOUS("Religious Holiday"),
    REGIONAL("Regional Holiday");

    private final String displayName;

    HolidayType(String displayName) {
        this.displayName = displayName;
    }

}
