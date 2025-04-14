package com.wfm.experts.hr.recruitmentonboarding.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wfm.experts.hr.recruitmentonboarding.enums.EmploymentType;
import com.wfm.experts.hr.recruitmentonboarding.enums.ExperienceLevel;
import com.wfm.experts.hr.recruitmentonboarding.enums.JobStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class JobDto {

    private Long id;

    private String title;
    private String position;
    private String description;
    private Integer openings;
    private BigDecimal annualSalary;

    private EmploymentType employmentType;
    private ExperienceLevel experienceLevel;
    private Integer yearsOfExperience;

    private JobStatus status;
    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
}
