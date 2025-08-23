package com.wfm.experts.tenant.common.employees.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wfm.experts.tenant.common.employees.enums.EmploymentStatus;
import com.wfm.experts.tenant.common.employees.enums.EmploymentType;
import com.wfm.experts.tenant.common.employees.enums.WorkMode;
import com.wfm.experts.validation.groups.OnEmployeeProfile; // Import group
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default; // Import Default
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employee_employment_details")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EmploymentDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // Fields always required (part of Default group, thus included in OnAdminCreation and OnEmployeeProfile)
    @NotNull(message = "Date of Joining is required", groups = {Default.class, OnEmployeeProfile.class}) // OnAdminCreation inherits Default
    @Column(name = "date_of_joining", nullable = false)
    private LocalDate dateOfJoining;

    @NotNull(message = "Employment Type is required", groups = {Default.class, OnEmployeeProfile.class})
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false)
    private EmploymentType employmentType;

    @NotNull(message = "Employment Status is required", groups = {Default.class, OnEmployeeProfile.class})
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false)
    private EmploymentStatus employmentStatus;

    @NotNull(message = "Notice Period (Days) is required", groups = {Default.class, OnEmployeeProfile.class})
    @Min(value = 0, message = "Notice period cannot be negative", groups = {Default.class, OnEmployeeProfile.class})
    @Column(name = "notice_period_days", nullable = false)
    private Integer noticePeriodDays;

    @NotNull(message = "Work Mode is required", groups = {Default.class, OnEmployeeProfile.class})
    @Enumerated(EnumType.STRING)
    @Column(name = "work_mode", nullable = false)
    private WorkMode workMode;

    // Fields that might be optional for admin creation but required for a full employee profile
    // Or they might just be optional always, depending on your business rules.
    // If they become mandatory for a full profile, add `groups = OnEmployeeProfile.class`
    @Column(name = "confirmation_date") // Nullable in DB
    private LocalDate confirmationDate; // Example: Not strictly needed for initial admin

    @Min(value = 0, message = "Probation period cannot be negative", groups = OnEmployeeProfile.class) // Example: Only validate for full profile
    @Column(name = "probation_period_months") // Nullable in DB
    private Integer probationPeriodMonths; // Example: Not strictly needed for initial admin
}