package com.wfm.experts.setup.wfm.shift.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
//    private Boolean weeklyOff;
    @JsonIgnore
    private String createdAt;
    @JsonIgnore
    private String updatedAt;
}
