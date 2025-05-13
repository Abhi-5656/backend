package com.wfm.experts.entity.tenant.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wfm.experts.validation.groups.OnAdminCreation;   // Import group
import com.wfm.experts.validation.groups.OnEmployeeProfile; // Import group
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.groups.Default; // Import Default
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employee_job_context_details")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class JobContextDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // These fields are likely always required, for admin and full employee profiles.
    @NotBlank(message = "Department is required", groups = {Default.class, OnEmployeeProfile.class}) // OnAdminCreation inherits Default
    @Column(name = "department_name", nullable = false)
    private String departmentName;

    @NotBlank(message = "Job Grade/Band is required", groups = {Default.class, OnEmployeeProfile.class})
    @Column(name = "job_grade_band", nullable = false)
    private String jobGradeBand;

    @NotBlank(message = "Cost Center is required", groups = {Default.class, OnEmployeeProfile.class})
    @Column(name = "cost_center", nullable = false)
    private String costCenter;

    // Organizational Role Description might be optional initially, or become more detailed later.
    // If it's truly optional always, no validation annotation is needed for presence.
    // If required for a full profile but not admin: @NotBlank(groups = OnEmployeeProfile.class)
    @Column(name = "organizational_role_description", columnDefinition = "TEXT") // Nullable in DB by default if no @NotNull/@NotBlank
    private String organizationalRoleDescription;
}