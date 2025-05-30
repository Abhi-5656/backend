package com.wfm.experts.setup.wfm.paypolicy.mapper;

import com.wfm.experts.setup.wfm.paypolicy.entity.Break;
import com.wfm.experts.setup.wfm.paypolicy.dto.BreakDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BreakMapper {
    BreakDTO toDto(Break entity);
    Break toEntity(BreakDTO dto);
}
