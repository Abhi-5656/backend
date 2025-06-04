//package com.wfm.experts.util;
//
//import com.wfm.experts.setup.wfm.paypolicy.enums.RoundingType;
//
//import java.time.LocalDateTime;
//
//public class TimeRoundingUtil {
//    public static LocalDateTime round(
//            LocalDateTime original,
//            int interval,
//            RoundingType type,
//            Integer gracePeriod // Can be null
//    ) {
//        if (interval <= 0) return original;
//
//        int minutes = original.getMinute();
//        int totalMinutes = minutes + original.getNano() / 60_000_000_000;
//        int roundedMinutes = switch (type) {
//            case UP -> ((minutes + interval - 1) / interval) * interval;
//            case DOWN -> (minutes / interval) * interval;
//            case NEAREST -> ((minutes + interval / 2) / interval) * interval;
//        };
//        // Apply grace period logic: If within grace, do not round
//        if (gracePeriod != null && Math.abs(roundedMinutes - minutes) <= gracePeriod) {
//            return original;
//        }
//        return original.withMinute(roundedMinutes).withSecond(0).withNano(0);
//    }
//}
