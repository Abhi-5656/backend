package com.wfm.experts.setup.wfm.paypolicy.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "night_allowance_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NightAllowanceRules {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean enabled;

    @Column(name = "start_time", length = 10)
    private String startTime; // e.g., "22:00"

    @Column(name = "end_time", length = 10)
    private String endTime;   // e.g., "06:00"

    @Column(name = "pay_multiplier")
    private Double payMultiplier;
}