package com.wfm.experts.setup.wfm.leavepolicy.entity;

import com.wfm.experts.setup.wfm.leavepolicy.enums.CalculationDateType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "lp_calculation_date_config")
@Getter
@Setter
public class CalculationDateConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_enabled", nullable = false)
    private boolean enabled;

    @Enumerated(EnumType.STRING)
    private CalculationDateType calculationType;

    private LocalDate customDate;
}