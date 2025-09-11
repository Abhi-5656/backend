package com.wfm.experts.setup.wfm.requesttype.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "validation_config")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ValidationConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean enabled;
    private boolean sandwich;
    private boolean holidayCount;
    private boolean overlap;
    private boolean probation;
    private boolean attachmentMandatory;
    private int attachmentDays;
}
