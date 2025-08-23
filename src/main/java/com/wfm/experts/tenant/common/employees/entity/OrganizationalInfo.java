package com.wfm.experts.tenant.common.employees.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wfm.experts.validation.groups.OnEmployeeProfile; // Import group
import jakarta.persistence.*;
import jakarta.validation.Valid; // For cascading validation
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
@Table(name = "employee_organizational_info")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class OrganizationalInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // EmploymentDetails is fundamental for organizational context.
    // It should exist for both admin and full employee profiles.
    @NotNull(message = "Employment details are required", groups = {Default.class, OnEmployeeProfile.class}) // OnAdminCreation inherits Default
    @Valid // Cascade validation to EmploymentDetails using its grouped annotations
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "employment_details_id", referencedColumnName = "id", unique = true)
    private EmploymentDetails employmentDetails;

    // JobContextDetails is also fundamental.
    @NotNull(message = "Job context details are required", groups = {Default.class, OnEmployeeProfile.class}) // OnAdminCreation inherits Default
    @Valid // Cascade validation to JobContextDetails using its grouped annotations
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "job_context_details_id", referencedColumnName = "id", unique = true)
    private JobContextDetails jobContextDetails;

    // This date signifies when this specific organizational assignment/context becomes effective.
    // It's likely required for any organizational setup.
    @NotNull(message = "Organizational assignment effective date is required", groups = {Default.class, OnEmployeeProfile.class})
    @Column(name = "org_assignment_effective_date", nullable = false)
    private LocalDate orgAssignmentEffectiveDate;

}