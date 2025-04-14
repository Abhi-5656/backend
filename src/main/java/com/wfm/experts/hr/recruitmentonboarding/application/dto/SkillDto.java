package com.wfm.experts.hr.recruitmentonboarding.application.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillDto {
    private String name;
    private String level;
    private Integer yearsOfExperience;
    private String badgeColor;
}
