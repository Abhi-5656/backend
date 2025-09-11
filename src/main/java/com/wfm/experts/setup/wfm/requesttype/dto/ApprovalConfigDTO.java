package com.wfm.experts.setup.wfm.requesttype.dto;

import com.wfm.experts.setup.wfm.requesttype.enums.AutoActionType;
import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ApprovalConfigDTO {
    private Long id;
    private boolean enabled;
    private boolean requires;
    private List<String> chainSteps;
    private List<String> notify;
    private int escalate;
    private AutoActionType autoAction;
    private int autoDays;
}
