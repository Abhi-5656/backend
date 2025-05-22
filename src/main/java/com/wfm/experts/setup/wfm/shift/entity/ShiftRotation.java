package com.wfm.experts.setup.wfm.shift.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "shift_rotations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftRotation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rotation_name", nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Integer weeks;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "shift_rotation_shifts",
            joinColumns = @JoinColumn(name = "shift_rotation_id"),
            inverseJoinColumns = @JoinColumn(name = "shift_id")
    )
    private List<Shift> shifts;

    @ElementCollection
    @CollectionTable(name = "shift_rotation_sequence", joinColumns = @JoinColumn(name = "shift_rotation_id"))
    @Column(name = "sequence")
    private List<Integer> sequence;

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
