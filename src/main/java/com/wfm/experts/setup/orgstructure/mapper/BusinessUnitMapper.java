package com.wfm.experts.setup.orgstructure.mapper;

import com.wfm.experts.setup.orgstructure.dto.BusinessUnitDto;
import com.wfm.experts.setup.orgstructure.entity.BusinessUnit;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BusinessUnitMapper {
    BusinessUnitDto toDto(BusinessUnit unit);
    List<BusinessUnitDto> toDtoList(List<BusinessUnit> units);

    BusinessUnit toEntity(BusinessUnitDto dto);
}
