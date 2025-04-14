package com.wfm.experts.hr.recruitmentonboarding.application.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Skill {

    private String name;

    private String level; // Beginner, Intermediate, Expert

    private Integer yearsOfExperience;

    private String badgeColor; // e.g. #34D399 or 'bg-green-100 text-green-800'
}
