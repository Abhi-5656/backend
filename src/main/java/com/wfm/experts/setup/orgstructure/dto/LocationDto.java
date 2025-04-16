package com.wfm.experts.setup.orgstructure.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

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

    private Long businessUnitId;
    private Long parentId;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Long> jobTitleIds;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<LocationDto> children;

    // Optional: remove this if not used
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private boolean root;
}
