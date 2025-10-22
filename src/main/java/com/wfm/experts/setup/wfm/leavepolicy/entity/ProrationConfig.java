package com.wfm.experts.setup.wfm.leavepolicy.entity;

import com.wfm.experts.setup.wfm.leavepolicy.enums.ProrationCutoffUnit; // Import new enum
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "lp_proration_config")
@Getter
@Setter
public class ProrationConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean isEnabled;

    @Enumerated(EnumType.STRING) // Add enum mapping
    private ProrationCutoffUnit cutoffUnit; // Add new field

    private Integer cutoffValue; // Rename field (was cutoffDay)

    private Integer grantPercentageBeforeCutoff;
    private Integer grantPercentageAfterCutoff;
}