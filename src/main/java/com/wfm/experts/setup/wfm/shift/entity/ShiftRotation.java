package com.wfm.experts.setup.wfm.shift.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shift_rotations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShiftRotation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rotation_name", nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Integer weeks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        isActive = true;
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
