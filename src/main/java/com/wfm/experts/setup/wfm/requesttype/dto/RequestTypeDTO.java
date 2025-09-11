package com.wfm.experts.setup.wfm.requesttype.dto;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RequestTypeDTO {
    private Long id;  // null for create, populated for read
    private String name;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;

    private ApprovalConfigDTO approval;
    private ClubbingConfigDTO clubbing;
    private ValidationConfigDTO validation;
    private NotificationConfigDTO notifications;
}