package com.wfm.experts.tenant.common.employees.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO {

    private String addressLine1;
    private String addressLine2;
    private String state;
    private String city;
    private String pincode;
}