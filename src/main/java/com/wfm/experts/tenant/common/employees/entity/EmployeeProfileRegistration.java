package com.wfm.experts.tenant.common.employees.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_profile_registrations",
        uniqueConstraints = {
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

    @Column(name = "employee_id", nullable = false, length = 64)
    private String employeeId;

    @JdbcTypeCode(Types.LONGVARCHAR)
    @Column(name = "employee_image_data", columnDefinition = "TEXT", nullable = false)
    private String  employeeImageData;

    @Column(name = "has_registered_with_image", nullable = false)
    private boolean hasRegisteredWithImage = false;

    @Lob
    @Column(name = "face_embedding")
    private byte[] faceEmbedding; // Changed from float[] to byte[]

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

    private void updateRegistrationStatus() {
        this.hasRegisteredWithImage = (this.employeeImageData != null && !this.employeeImageData.isEmpty());
    }
}