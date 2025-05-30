package com.wfm.experts.setup.wfm.paypolicy.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "breaks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Break {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String name;

    private Integer duration;     // in minutes

    @Column(length = 10)
    private String startTime;     // e.g. "12:00"

    @Column(length = 10)
    private String endTime;       // e.g. "12:30"
}
