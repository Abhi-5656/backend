package com.wfm.experts.tenant.common.employees.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.time.LocalDateTime;

/**
 * Represents a record of an employee's profile registration,
 * specifically tracking whether a profile image has been provided.
 */
@Entity
@Table(name = "employee_profile_registrations",
        uniqueConstraints = {
                // The email constraint is removed, leaving employee_id as the unique key.
                @UniqueConstraint(name = "uc_epr_employee_id", columnNames = {"employee_id"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeProfileRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The unique identifier for the employee. Links to the main Employee record.
     */
    @Column(name = "employee_id", nullable = false, length = 64)
    private String employeeId;

    @JdbcTypeCode(Types.LONGVARCHAR)
    @Column(name = "employee_image_data", columnDefinition = "TEXT", nullable = false)
    private String  employeeImageData;

    /**
     * A flag that is automatically set to true if an image is provided.
     * This provides a quick way to check registration status without loading the image data.
     */
    @Column(name = "has_registered_with_image", nullable = false)
    private boolean hasRegisteredWithImage = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        updateRegistrationStatus();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        updateRegistrationStatus();
    }

    /**
     * Helper method to set the registration status based on the presence of image data.
     */
    private void updateRegistrationStatus() {
        this.hasRegisteredWithImage = (this.employeeImageData != null && !this.employeeImageData.isEmpty());
    }
}