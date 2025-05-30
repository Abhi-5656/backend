package com.wfm.experts.setup.wfm.paypolicy.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pre_shift_inclusion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreShiftInclusion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean enabled;
    private Integer fromValue;

    @Column(length = 10)
    private String fromUnit;  // "minutes" or "hours"

    private Integer upToValue;

    @Column(length = 10)
    private String upToUnit;  // "minutes" or "hours"
}
