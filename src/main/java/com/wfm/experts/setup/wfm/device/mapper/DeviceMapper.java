package com.wfm.experts.setup.wfm.device.mapper;

import com.wfm.experts.setup.wfm.device.dto.DeviceDTO;
import com.wfm.experts.setup.wfm.device.entity.Device;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface DeviceMapper {
    DeviceMapper INSTANCE = Mappers.getMapper(DeviceMapper.class);

    DeviceDTO toDto(Device device);

    Device toEntity(DeviceDTO deviceDTO);
}
