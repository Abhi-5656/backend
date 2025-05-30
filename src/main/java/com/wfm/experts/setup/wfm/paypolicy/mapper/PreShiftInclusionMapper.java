package com.wfm.experts.setup.wfm.paypolicy.mapper;

import com.wfm.experts.setup.wfm.paypolicy.entity.PreShiftInclusion;
import com.wfm.experts.setup.wfm.paypolicy.dto.PreShiftInclusionDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PreShiftInclusionMapper {
    PreShiftInclusionDTO toDto(PreShiftInclusion entity);
    PreShiftInclusion toEntity(PreShiftInclusionDTO dto);
}
