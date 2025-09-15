package com.wfm.experts.setup.wfm.requesttype.dto;

import com.wfm.experts.setup.wfm.requesttype.enums.ApprovalModeType;
import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ApprovalConfigDTO {

    private Long id;

    private boolean enabled;

    private ApprovalModeType mode;

    private List<String> chainSteps;

    private List<String> notify;

    private Integer escalate;

    private Integer autoDays;

}