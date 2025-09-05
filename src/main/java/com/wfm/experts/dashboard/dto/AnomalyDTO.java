package com.wfm.experts.dashboard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnomalyDTO {
    private String message;
    private String date;
}