package com.wfm.experts.setup.wfm.requesttype.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClubbingConfigDTO {
    private Long id;
    private boolean enabled;
    private List<String> with;
    private boolean weekends;
    private boolean hols;
    private int max;
}
