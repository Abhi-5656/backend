package com.wfm.experts.setup.wfm.shift.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Weekday {
    SUN, MON, TUE, WED, THU, FRI, SAT;

    @JsonCreator
    public static Weekday fromString(String value) {
        if (value == null) return null;
        switch (value.trim().toLowerCase()) {
            case "sun":
                return SUN;
            case "mon":
                return MON;
            case "tue":
                return TUE;
            case "wed":
                return WED;
            case "thu":
                return THU;
            case "fri":
                return FRI;
            case "sat":
                return SAT;
            default:
                throw new IllegalArgumentException(
                        "Invalid weekday: " + value +
                                ". Must be one of: Sun, Mon, Tue, Wed, Thu, Fri, Sat"
                );
        }
    }

    @JsonValue
    public String toJson() {
        return this.name();
    }

    public static Weekday from(java.time.DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case SUNDAY:
                return SUN;
            case MONDAY:
                return MON;
            case TUESDAY:
                return TUE;
            case WEDNESDAY:
                return WED;
            case THURSDAY:
                return THU;
            case FRIDAY:
                return FRI;
            case SATURDAY:
                return SAT;
            default:
                throw new IllegalArgumentException("Unsupported DayOfWeek: " + dayOfWeek);
        }
    }
}
