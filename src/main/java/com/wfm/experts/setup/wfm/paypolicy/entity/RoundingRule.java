package com.wfm.experts.setup.wfm.paypolicy.entity;

import com.wfm.experts.setup.wfm.paypolicy.enums.RoundingType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rounding_rule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoundingRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer interval; // minutes

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private RoundingType type;

    private Integer gracePeriod; // minutes
}
