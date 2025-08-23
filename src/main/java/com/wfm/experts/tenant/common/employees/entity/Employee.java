package com.wfm.experts.tenant.common.employees.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wfm.experts.setup.orgstructure.entity.BusinessUnit;
import com.wfm.experts.setup.orgstructure.entity.JobTitle;
import com.wfm.experts.setup.orgstructure.entity.Location;
import com.wfm.experts.validation.groups.OnEmployeeProfile; // Import group
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.validator.constraints.Length;
import jakarta.validation.Valid; // For cascading validation
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.groups.Default; // Import Default

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employees",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"email"}),
                @UniqueConstraint(columnNames = {"employee_id"}),
                @UniqueConstraint(columnNames = {"phone_number"})
        })
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Core Identifying and Auth Info (Generally always required) ---
    @NotBlank(message = "Employee Code (ID) is required", groups = {Default.class, OnEmployeeProfile.class}) // OnAdminCreation inherits Default
    @Length(max = 50, message = "Employee ID must not exceed 50 characters", groups = {Default.class, OnEmployeeProfile.class})
    @Column(name = "employee_id", nullable = false, unique = true, length = 50)
    private String employeeId;

    @NotBlank(message = "Work Email ID is required", groups = {Default.class, OnEmployeeProfile.class})
    @Email(message = "Invalid email format", groups = {Default.class, OnEmployeeProfile.class})
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    // Password validation (e.g., @Size) can also be group-specific if needed,
    // but @NotBlank is fundamental.
    @NotBlank(message = "Password is required", groups = {Default.class, OnEmployeeProfile.class}) // Consider if password can be updated separately
    @Column(name = "password", nullable = false)
    private String password;

    @NotBlank(message = "Primary Mobile Number is required", groups = {Default.class, OnEmployeeProfile.class})
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,20}$", message = "Invalid mobile number format for primary mobile", groups = {Default.class, OnEmployeeProfile.class})
    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;

    // --- System Role ---
//    @NotNull(message = "System Role is required", groups = {Default.class, OnEmployeeProfile.class})
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "role_id", nullable = false)
//    private Role role;
    // --- System Roles (One Employee can have Many Roles) ---
    @NotNull(message = "System Role is required", groups = {Default.class, OnEmployeeProfile.class})
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "employee_roles",
            joinColumns = @JoinColumn(name = "employee_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private List<Role> roles = new ArrayList<>();


    // --- Personal Information (Validation cascaded based on active group) ---
    @NotNull(message = "Personal information is required", groups = {Default.class, OnEmployeeProfile.class}) // Ensures PersonalInfo object exists
    @Valid // This enables cascading validation to PersonalInfo's grouped constraints
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_info_id", referencedColumnName = "id")
    private PersonalInfo personalInfo;

    // --- Organizational Information (Validation cascaded based on active group) ---
    @NotNull(message = "Organizational information is required", groups = {Default.class, OnEmployeeProfile.class}) // Ensures OrgInfo object exists
    @Valid // This enables cascading validation to OrganizationalInfo's grouped constraints
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "organizational_info_id", referencedColumnName = "id")
    private OrganizationalInfo organizationalInfo;

    // --- Organizational Structure & Job Relationships (These are often set later for a full profile) ---
    // If these are mandatory for a full employee profile, use groups = OnEmployeeProfile.class
    // For admin creation, they might be null.
    @NotNull(message = "Work Location is required for a full profile", groups = OnEmployeeProfile.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_location_id") // DB column should be nullable
    private Location workLocation;

    @NotNull(message = "Business Unit is required for a full profile", groups = OnEmployeeProfile.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_unit_id") // DB column should be nullable
    private BusinessUnit businessUnit;

    @NotNull(message = "Designation (Job Title) is required for a full profile", groups = OnEmployeeProfile.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_title_id") // DB column should be nullable
    private JobTitle jobTitle;

    // Managers might be optional or set later.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporting_manager_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private Employee reportingManager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hr_manager_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private Employee hrManager;


    // --- Tenant and Timestamps ---
    @JsonIgnore
    @Column(name = "tenant_id")
    private String tenantId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;


    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date();
    }
}