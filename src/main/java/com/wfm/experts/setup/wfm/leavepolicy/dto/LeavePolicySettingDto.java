package com.wfm.experts.setup.wfm.leavepolicy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LeavePolicySettingDto {

    @NotNull(message = "Policy ID cannot be null.")
    private Long policyId;

    @NotNull(message = "Visibility cannot be null.")
    private String visibility; // "visible" or "hidden"
}