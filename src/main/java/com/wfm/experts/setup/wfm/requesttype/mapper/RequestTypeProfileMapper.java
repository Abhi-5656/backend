package com.wfm.experts.setup.wfm.requesttype.mapper;

import com.wfm.experts.setup.wfm.requesttype.dto.RequestTypeProfileDTO;
import com.wfm.experts.setup.wfm.requesttype.entity.RequestType;
import com.wfm.experts.setup.wfm.requesttype.entity.RequestTypeProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RequestTypeProfileMapper {

    // entity → dto
    @Mapping(target = "requestTypeIds", expression = "java(mapRequestTypesToIds(entity.getRequestTypes()))")
    RequestTypeProfileDTO toDto(RequestTypeProfile entity);

    // dto → entity (will set requestTypes in service layer, not here)
    @Mapping(target = "requestTypes", ignore = true) // handled manually after fetch
    RequestTypeProfile toEntity(RequestTypeProfileDTO dto);

    // helper for mapping requestTypes -> ids
    default List<Long> mapRequestTypesToIds(Set<RequestType> requestTypes) {
        if (requestTypes == null) {
            return null;
        }
        return requestTypes.stream()
                .map(RequestType::getId)
                .collect(Collectors.toList());
    }
}
