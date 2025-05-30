package com.wfm.experts.setup.wfm.paypolicy.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BreakDTO {
    private Long id;
    private String name;
    private Integer duration;
    private String startTime;
    private String endTime;
}
