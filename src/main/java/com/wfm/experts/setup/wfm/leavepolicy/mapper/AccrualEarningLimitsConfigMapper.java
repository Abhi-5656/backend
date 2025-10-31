package com.wfm.experts.setup.wfm.leavepolicy.mapper;

import com.wfm.experts.setup.wfm.leavepolicy.dto.AccrualEarningLimitsConfigDto;
import com.wfm.experts.setup.wfm.leavepolicy.entity.AccrualEarningLimitsConfig;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccrualEarningLimitsConfigMapper {
    AccrualEarningLimitsConfigDto toDto(AccrualEarningLimitsConfig entity);
    AccrualEarningLimitsConfig toEntity(AccrualEarningLimitsConfigDto dto);
}