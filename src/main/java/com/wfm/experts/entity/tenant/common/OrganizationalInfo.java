package com.wfm.experts.entity.tenant.common;

import com.wfm.experts.entity.tenant.common.enums.EmploymentStatus;
import com.wfm.experts.entity.tenant.common.enums.EmploymentTypeEnum;
import com.wfm.experts.entity.tenant.common.enums.WorkMode;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "employee_organizational_info") // Name of the new database table
public class OrganizationalInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Fields from EmploymentDetails are now direct fields of OrganizationalInfo
    // Employment Details
    @NotNull(message = "Date of Joining is required")
    @Column(name = "date_of_joining") // Corresponds to EmploymentDetails.dateOfJoining
    private LocalDate dateOfJoining;

    @NotNull(message = "Employment Type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type") // Corresponds to EmploymentDetails.employmentType
    private EmploymentTypeEnum employmentType;

    @NotNull(message = "Employment Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status") // Corresponds to EmploymentDetails.employmentStatus
    private EmploymentStatus employmentStatus;

    @Column(name = "confirmation_date") // Corresponds to EmploymentDetails.confirmationDate
    private LocalDate confirmationDate;

    @Column(name = "probation_period_months") // Corresponds to EmploymentDetails.probationPeriodMonths
    @Min(value = 0, message = "Probation period cannot be negative")
    private Integer probationPeriodMonths;

    @NotNull(message = "Notice Period is required")
    @Column(name = "notice_period_days") // Corresponds to EmploymentDetails.noticePeriodDays
    @Min(value = 0, message = "Notice period cannot be negative")
    private Integer noticePeriodDays;

    @NotNull(message = "Work Mode is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "work_mode") // Corresponds to EmploymentDetails.workMode
    private WorkMode workMode;

    // Departmental & Job Information (fields that are descriptive strings or simple types)
    @NotBlank(message = "Department is required")
    @Column(name = "department_name") // Corresponds to EmploymentDetails.departmentName
    private String departmentName;

    @NotBlank(message = "Job Grade/Band is required")
    @Column(name = "job_grade_band") // Corresponds to EmploymentDetails.jobGradeBand
    private String jobGradeBand;

    @NotBlank(message = "Cost Center is required")
    @Column(name = "cost_center") // Corresponds to EmploymentDetails.costCenter
    private String costCenter;

    // Descriptive organizational role, distinct from the system Role.
    @Column(name = "organizational_role_description") // Corresponds to EmploymentDetails.organizationalRoleDescription
    private String organizationalRoleDescription;

    // Role Effective Date (for the system Role assigned in the main Employee entity)
    @NotNull(message = "Role Effective Date is required")
    @Column(name = "role_effective_date") // Corresponds to EmploymentDetails.roleEffectiveDate
    private LocalDate roleEffectiveDate;


    // Note: The Employee entity will hold the foreign key to this OrganizationalInfo table.
    // This OrganizationalInfo entity does not have a direct reference back to Employee
    // for a unidirectional mapping owned by Employee.

    // Other fields that are purely part of OrganizationalInfo and not in EmploymentDetails
    // could be added directly here if any.
    // For example, if there was a field "OfficeBranchCode":
    // @Column(name = "office_branch_code")
    // private String officeBranchCode;
}
