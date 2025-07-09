// AllowedFileTypesMapper.java
package com.wfm.experts.setup.wfm.leavepolicy.mapper;

import com.wfm.experts.setup.wfm.leavepolicy.dto.AllowedFileTypesDTO;
import com.wfm.experts.setup.wfm.leavepolicy.entity.AllowedFileTypes;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AllowedFileTypesMapper {
    AllowedFileTypesDTO toDto(AllowedFileTypes entity);
    AllowedFileTypes toEntity(AllowedFileTypesDTO dto);
}
