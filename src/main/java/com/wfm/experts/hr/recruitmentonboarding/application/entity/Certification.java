package com.wfm.experts.hr.recruitmentonboarding.application.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.LocalDate;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certification {

    private String name;

    private String issuingOrganization;

    private LocalDate issueDate;

    private LocalDate expiryDate;

    private String badgeColor; // Optional visual metadata
}
