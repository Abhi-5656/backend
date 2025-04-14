package com.wfm.experts.hr.recruitmentonboarding.application.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "job_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "job_id")
    private Long jobId;

    // 🔹 Personal Info
    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    // 🔹 Address
    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "pincode")
    private String pincode;

    // 🔹 Experience
    @Column(name = "total_experience")
    private Integer totalExperience;

    @Column(name = "current_company")
    private String currentCompany;

    @Column(name = "current_ctc")
    private String currentCTC;

    @Column(name = "expected_ctc")
    private String expectedCTC;

    @Column(name = "notice_period")
    private String noticePeriod;

    // 🔹 Education
    @Column(name = "degree")
    private String degree;

    @Column(name = "specialization")
    private String specialization;

    @Column(name = "university")
    private String university;

    @Column(name = "passing_year")
    private Integer passingYear;

    // 🔹 Resume
    @Column(name = "resume_url")
    private String resumeUrl;

    // 🔹 Skills
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "job_application_skills", joinColumns = @JoinColumn(name = "application_id"))
    private List<Skill> skills;

    // 🔹 Certifications
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "job_application_certifications", joinColumns = @JoinColumn(name = "application_id"))
    private List<Certification> certifications;

    // 🔹 Metadata
    @Column(name = "applied_date")
    private LocalDate appliedDate;

    @PrePersist
    public void prePersist() {
        this.appliedDate = LocalDate.now();
    }
}
