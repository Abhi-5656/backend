package com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.entity;

import com.wfm.experts.setup.wfm.holiday.entity.HolidayProfile;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "holiday_profile_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HolidayProfileAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many assignments -> One holiday profile
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "holiday_profile_id", nullable = false)
    private HolidayProfile holidayProfile;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

   private String employeeId;
}
