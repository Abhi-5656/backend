package com.wfm.experts.setup.wfm.holiday.dto;

import com.wfm.experts.setup.wfm.holiday.entity.Holiday;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "Profile name cannot be blank")
    private String profileName;

    private List<Holiday> holidays; // Holidays are assigned later, not validated at profile creation

    private Boolean isActive = true;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}