package com.wfm.experts.tenant.common.employees.dto;

import com.wfm.experts.tenant.common.employees.enums.Relationship;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyContactDTO {

    @NotBlank(message = "Emergency contact name is required")
    private String contactName;

    @NotBlank(message = "Emergency contact number is required")
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,20}$", message = "Invalid mobile number format")
    private String contactNumber;

    private Relationship relationship;
}