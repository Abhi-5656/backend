package com.wfm.experts.setup.wfm.shift.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ShiftRotationDTO {
    private Long id;
    private String name;
    private Integer weeks;
    private List<ShiftDTO> shifts;     // Use ShiftDTO for nested shifts
    private List<Integer> sequence;
    private String createdAt;
    private String updatedAt;
    private Boolean isActive;
}
