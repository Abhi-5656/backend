package com.wfm.experts.setup.wfm.leavepolicy.mapper;

import com.wfm.experts.setup.wfm.leavepolicy.dto.CalculationDateConfigDto;
import com.wfm.experts.setup.wfm.leavepolicy.entity.CalculationDateConfig;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CalculationDateConfigMapper {
    CalculationDateConfigDto toDto(CalculationDateConfig entity);
    CalculationDateConfig toEntity(CalculationDateConfigDto dto);
}