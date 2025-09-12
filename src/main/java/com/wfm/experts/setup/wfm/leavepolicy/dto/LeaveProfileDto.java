package com.wfm.experts.setup.wfm.leavepolicy.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class LeaveProfileDto {

    private Long id;

    @NotBlank(message = "Profile name cannot be blank.")
    private String profileName;

    @NotEmpty(message = "At least one leave policy must be selected.")
    @Valid
    private List<LeavePolicySettingDto> leavePolicySettings;
}