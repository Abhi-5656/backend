package com.wfm.experts.tenant.common.employees.dto;

import com.wfm.experts.tenant.common.employees.enums.Relationship;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyContactDTO {

    private String contactName;
    private String contactNumber;
    private Relationship relationship;
}