package com.wfm.experts.setup.wfm.requesttype.mapper;

import com.wfm.experts.setup.wfm.requesttype.dto.ClubbingConfigDTO;
import com.wfm.experts.setup.wfm.requesttype.entity.ClubbingConfig;
import org.mapstruct.*;

@Mapper(config = MappersConfig.class)
public interface ClubbingConfigMapper {

    ClubbingConfigDTO toDto(ClubbingConfig entity);

    @Named("newClubbingConfig")
    @Mapping(target = "id", ignore = true)
    ClubbingConfig toNewEntity(ClubbingConfigDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget ClubbingConfig target, ClubbingConfigDTO source);
}
