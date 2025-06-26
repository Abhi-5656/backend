package com.wfm.experts.setup.wfm.holiday.dto;

import com.wfm.experts.setup.wfm.holiday.entity.Holiday;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HolidayProfileDTO implements Serializable {

    private Long id;

    private String profileName;

    private List<Holiday> holidays; // You can use List<HolidayDTO> if you prefer DTOs throughout

    private Boolean isActive = true;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
