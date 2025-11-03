package com.wfm.experts.setup.wfm.holiday.dto;

import com.wfm.experts.setup.wfm.holiday.enums.HolidayType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank(message = "Holiday name cannot be blank")
    private String holidayName;

    @NotNull(message = "Holiday type cannot be null")
    private HolidayType holidayType;

    @NotNull(message = "Start date cannot be null")
    private LocalDate startDate;

    @NotNull(message = "End date cannot be null")
    private LocalDate endDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}