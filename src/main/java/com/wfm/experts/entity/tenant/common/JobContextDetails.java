package com.wfm.experts.entity.tenant.common; // Assuming this is your common entity package

import jakarta.persistence.Column;
import jakarta.persistence.Entity; // Changed from @Embeddable
import jakarta.persistence.GeneratedValue; // For ID generation
import jakarta.persistence.GenerationType; // For ID generation
import jakarta.persistence.Id; // For primary key
import jakarta.persistence.Table; // To specify table name
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Entity class representing specific departmental and job context details for an employee.
 * This class now corresponds to its own database table.
 */
@Getter
@Setter
@NoArgsConstructor // Lombok: Adds a no-argument constructor
@AllArgsConstructor // Lombok: Adds an all-argument constructor
@Entity // Changed from @Embeddable
@Table(name = "employee_job_context_details") // Specifies the database table name
public class JobContextDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // Primary key for this new table

    @NotBlank(message = "Department is required")
    @Column(name = "department_name")
    private String departmentName;

    @NotBlank(message = "Job Grade/Band is required")
    @Column(name = "job_grade_band")
    private String jobGradeBand;

    @NotBlank(message = "Cost Center is required")
    @Column(name = "cost_center")
    private String costCenter;

    @Column(name = "organizational_role_description") // Descriptive text for the organizational role
    private String organizationalRoleDescription;

    // For a unidirectional mapping where OrganizationalInfo owns the relationship,
    // JobContextDetails does not need a field linking back to OrganizationalInfo.
}
