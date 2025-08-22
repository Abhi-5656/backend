package com.wfm.experts.setup.wfm.leavepolicy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LimitsConfigDto {
    private boolean enabled;

    @Valid
    private CarryForwardConfigDto carryForward;

    @Valid
    private EncashmentConfigDto encashment;

    @Valid
    private EligibilityConfigDto eligibility;
}