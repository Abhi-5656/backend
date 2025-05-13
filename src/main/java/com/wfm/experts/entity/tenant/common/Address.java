package com.wfm.experts.entity.tenant.common;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class Address {

    @NotBlank(message = "Address Line 1 is required")
    @Column(name = "address_line_1") // Will be prefixed for current/permanent
    private String addressLine1;

    @Column(name = "address_line_2") // Will be prefixed
    private String addressLine2;

    @NotBlank(message = "State is required")
    @Column(name = "state") // Will be prefixed
    private String state;

    @NotBlank(message = "City is required")
    @Column(name = "city") // Will be prefixed
    private String city;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid pincode format")
    @Column(name = "pincode") // Will be prefixed
    private String pincode;
}