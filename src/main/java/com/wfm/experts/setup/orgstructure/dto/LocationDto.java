package com.wfm.experts.setup.orgstructure.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationDto {

    private Long id;
    private String name;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expirationDate;

    private String color;

    private BusinessUnitDto businessUnit; // Modified from Long to DTO

    private Long parentId;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<JobTitleDto> jobTitles;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<LocationDto> children;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private boolean root;
}
