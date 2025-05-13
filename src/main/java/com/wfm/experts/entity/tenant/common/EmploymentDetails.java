package com.wfm.experts.entity.tenant.common; // Assuming this is your common entity package

import com.wfm.experts.entity.tenant.common.enums.EmploymentStatus;
import com.wfm.experts.entity.tenant.common.enums.EmploymentType;
import com.wfm.experts.entity.tenant.common.enums.WorkMode;
import jakarta.persistence.*; // Import for Entity, Table, Id, GeneratedValue etc.
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

/**
 * Entity class representing specific details about an employee's terms and
 * status of employment. This class now corresponds to its own database table.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity // Changed from @Embeddable
@Table(name = "employee_employment_details") // Name for the new database table
public class EmploymentDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // Primary key for this new table

    @NotNull(message = "Date of Joining is required")
    @Column(name = "date_of_joining")
    private LocalDate dateOfJoining;

    @NotNull(message = "Employment Type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type")
    private EmploymentType employmentType; // Using EmploymentTypeEnum from org_info_enums_v1

    @NotNull(message = "Employment Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status")
    private EmploymentStatus employmentStatus; // Using EmploymentStatus from org_info_enums_v1

    @Column(name = "confirmation_date")
    private LocalDate confirmationDate;

    @Column(name = "probation_period_months")
    @Min(value = 0, message = "Probation period cannot be negative")
    private Integer probationPeriodMonths;

    @NotNull(message = "Notice Period (Days) is required")
    @Column(name = "notice_period_days")
    @Min(value = 0, message = "Notice period cannot be negative")
    private Integer noticePeriodDays;

    @NotNull(message = "Work Mode is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "work_mode")
    private WorkMode workMode; // Using WorkMode from org_info_enums_v1

    // For a unidirectional mapping where OrganizationalInfo owns the relationship to EmploymentDetails,
    // EmploymentDetails does not need a field linking back to OrganizationalInfo.
}
