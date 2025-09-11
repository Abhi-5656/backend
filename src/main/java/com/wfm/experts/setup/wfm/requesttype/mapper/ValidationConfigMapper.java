package com.wfm.experts.setup.wfm.requesttype.mapper;

import com.wfm.experts.setup.wfm.requesttype.dto.ValidationConfigDTO;
import com.wfm.experts.setup.wfm.requesttype.entity.ValidationConfig;
import org.mapstruct.*;

@Mapper(config = MappersConfig.class)
public interface ValidationConfigMapper {

    ValidationConfigDTO toDto(ValidationConfig entity);

    @Named("newValidationConfig")
    @Mapping(target = "id", ignore = true)
    ValidationConfig toNewEntity(ValidationConfigDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget ValidationConfig target, ValidationConfigDTO source);
}
