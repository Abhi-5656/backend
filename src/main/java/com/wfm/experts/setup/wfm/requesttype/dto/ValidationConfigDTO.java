package com.wfm.experts.setup.wfm.requesttype.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ValidationConfigDTO {
    private Long id;
    private boolean enabled;
    private boolean sandwich;
    private boolean holidayCount;
    private boolean overlap;
    private boolean probation;
    private boolean attachmentMandatory;
    private int attachmentDays;
}
