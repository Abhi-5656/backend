package com.wfm.experts.setup.wfm.leavepolicy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wfm.experts.setup.wfm.leavepolicy.enums.CalculationDateType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CalculationDateConfigDto {
    private boolean enabled;
    private CalculationDateType calculationType;
    private LocalDate customDate;
}