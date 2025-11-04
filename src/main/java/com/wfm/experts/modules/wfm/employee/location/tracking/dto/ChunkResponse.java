package com.wfm.experts.modules.wfm.employee.location.tracking.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class ChunkResponse {
    private int chunkIndex;
    private int pointCount;
    private String startedAt;      // ISO
    private String endedAt;        // ISO
    private String geoJson;        // ST_AsGeoJSON(LineStringM)
}
