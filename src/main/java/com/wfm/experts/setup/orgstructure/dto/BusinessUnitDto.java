package com.wfm.experts.setup.orgstructure.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessUnitDto {
    private Long id;
    private String name;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;
    private String color;
}
