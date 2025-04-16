package com.wfm.experts.setup.orgstructure.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobTitleDto {

    private Long id;

    private String jobTitle;
    private String shortName;
    private String code;
    private Integer sortOrder;

    private LocalDate effectiveDate;
    private LocalDate expirationDate;

    private String color;
}
