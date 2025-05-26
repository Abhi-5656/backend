package com.wfm.experts.setup.wfm.holiday.dto;

import com.wfm.experts.setup.wfm.holiday.enums.HolidayType;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HolidayDTO implements Serializable {

    private Long id;

    private String holidayName;

    private HolidayType holidayType;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
