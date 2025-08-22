package com.wfm.experts.setup.wfm.leavepolicy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wfm.experts.setup.wfm.leavepolicy.enums.Applicability;
import com.wfm.experts.setup.wfm.leavepolicy.enums.LeaveType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

/**
 * The main DTO for creating and returning Leave Policy data.
 * It includes validation rules for incoming API requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LeavePolicyDto {

    private Long id;

    @NotBlank(message = "Policy name cannot be blank.")
    @Size(max = 100, message = "Policy name must be less than 100 characters.")
    private String policyName;

    @Size(max = 20, message = "Leave code must be less than 20 characters.")
    private String leaveCode;

    @NotNull(message = "Effective date is required.")
    private LocalDate effectiveDate;

    private LocalDate expirationDate;

    @NotNull(message = "Leave type is required.")
    private LeaveType leaveType;

    @NotEmpty(message = "Applicability must be specified.")
    private List<Applicability> applicableFor;

    @NotBlank(message = "Leave color is required.")
    private String leaveColor;

    @Valid // This annotation cascades validation to the nested object.
    private CalculationDateConfigDto calculationDateConfig;

    @Valid
    private GrantsConfigDto grantsConfig;

    @Valid
    private LimitsConfigDto limitsConfig;

    @Valid
    private AttachmentsConfigDto attachmentsConfig;
}