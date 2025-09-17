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

    @Mapping(target = "requestTypeIds", expression = "java(mapRequestTypesToIds(entity.getRequestTypes()))")
    RequestTypeProfileDTO toDto(RequestTypeProfile entity);

    default List<Long> mapRequestTypesToIds(Set<RequestType> requestTypes) {
        if (requestTypes == null) {
            return null;
        }
        return requestTypes.stream()
                .map(RequestType::getId)
                .collect(Collectors.toList());
    }
}