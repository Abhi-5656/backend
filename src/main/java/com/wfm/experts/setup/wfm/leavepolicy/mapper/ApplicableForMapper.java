// ApplicableForMapper.java
package com.wfm.experts.setup.wfm.leavepolicy.mapper;

import com.wfm.experts.setup.wfm.leavepolicy.dto.ApplicableForDTO;
import com.wfm.experts.setup.wfm.leavepolicy.entity.ApplicableFor;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApplicableForMapper {
    ApplicableForDTO toDto(ApplicableFor entity);
    ApplicableFor toEntity(ApplicableForDTO dto);
}
