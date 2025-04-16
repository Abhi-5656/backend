package com.wfm.experts.setup.orgstructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "job_titles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobTitle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_title", nullable = false)
    private String jobTitle;

    @Column(name = "short_name", nullable = false)
    private String shortName;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(length = 20)
    private String color;
}
