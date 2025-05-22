package com.wfm.experts.setup.wfm.shift.dto;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ShiftDTO {
    private Long id;
    private String shiftName;
    private String shiftLabel;
    private String color;
    private String startTime;      // "HH:mm"
    private String endTime;        // "HH:mm"
    private Boolean isActive;
    private String calendarDate;   // "yyyy-MM-dd"
    private Boolean weeklyOff;
    private String createdAt;
    private String updatedAt;
}
