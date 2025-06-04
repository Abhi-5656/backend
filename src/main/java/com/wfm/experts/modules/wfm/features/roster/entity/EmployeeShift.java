package com.wfm.experts.modules.wfm.features.roster.entity;

import com.wfm.experts.setup.wfm.shift.entity.Shift;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "employee_shifts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeShift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String employeeId;

    // Shift can be null for weekly off or holiday
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = true)
    private Shift shift;

    @Column(name = "calendar_date")
    private LocalDate calendarDate;

    @Column(name = "is_week_off")
    private Boolean isWeekOff = false;

    @Column(name = "is_holiday")
    private Boolean isHoliday = false;

    @Column(name = "weekday", nullable = false, length = 10)
    private String weekday; // "MONDAY", "TUESDAY", etc.

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "assigned_by", length = 50)
    private String assignedBy;
}
