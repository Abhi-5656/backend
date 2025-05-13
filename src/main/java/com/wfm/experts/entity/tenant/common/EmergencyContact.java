package com.wfm.experts.entity.tenant.common;

import com.wfm.experts.entity.tenant.common.enums.Relationship;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class EmergencyContact {

    @NotBlank(message = "Emergency contact name is required")
    @Column(name = "emergency_contact_name")
    private String contactName;

    @NotBlank(message = "Emergency contact number is required")
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,20}$", message = "Invalid mobile number format")
    @Column(name = "emergency_contact_number")
    private String contactNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "emergency_contact_relationship")
    private Relationship relationship;
}