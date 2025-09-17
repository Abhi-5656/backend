package com.wfm.experts.setup.wfm.requesttype.dto;

import com.wfm.experts.setup.wfm.leavepolicy.dto.LeavePolicyDto;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class RequestTypeDTO {
    private Long id;
    private String name;
    private Date effectiveDate;
    private Date expirationDate;
    private Long leavePolicyId;
    private ApprovalConfigDTO approval;
    private ClubbingConfigDTO clubbing;
    private ValidationConfigDTO validation;
    private NotificationConfigDTO notifications;
}