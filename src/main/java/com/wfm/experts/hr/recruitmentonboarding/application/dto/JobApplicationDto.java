package com.wfm.experts.hr.recruitmentonboarding.application.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplicationDto {

    private Long id;
    private Long jobId;

    // 🔹 Personal Info
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    // 🔹 Address
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pincode;

    // 🔹 Experience Summary
    private Integer totalExperience;
    private String currentCompany;
    private String currentCTC;
    private String expectedCTC;
    private String noticePeriod;

    // 🔹 Education
    private String degree;
    private String specialization;
    private String university;
    private Integer passingYear;

    // 🔹 Resume
    private String resumeUrl;

    // 🔹 Skills & Certifications
    private List<SkillDto> skills;
    private List<CertificationDto> certifications;

    // 🔹 Submission Info
    private LocalDate appliedDate;
}
