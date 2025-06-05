package com.wfm.experts.setup.wfm.shift.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "shifts")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Shift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shift_name", nullable = false, unique = true)
    private String shiftName;

    @Column(name = "shift_label",nullable = false, unique = true)
    private String shiftLabel;


    private String color;

//    @Column(nullable = false)
//    private LocalTime startTime;
//
//    @Column(nullable = false)
//    private LocalTime endTime;
      @Column(nullable = false, updatable = false)
      private LocalTime startTime;

     @Column(nullable = false, updatable = false)
     private LocalTime endTime;


    private Boolean isActive = true;


    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

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
