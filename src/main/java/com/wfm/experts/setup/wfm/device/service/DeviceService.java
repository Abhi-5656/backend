package com.wfm.experts.setup.wfm.device.service;

import com.wfm.experts.setup.wfm.device.dto.DeviceDTO;
import com.wfm.experts.setup.wfm.device.enums.DeviceStatus;
import com.wfm.experts.setup.wfm.device.enums.DeviceType;

import java.util.List;
import java.util.Optional;

public interface DeviceService {

    DeviceDTO createDevice(DeviceDTO deviceDTO);

    DeviceDTO updateDevice(String deviceId, DeviceDTO deviceDTO);

    Optional<DeviceDTO> getDeviceById(String deviceId);

    List<DeviceDTO> getAllDevices();

    List<DeviceDTO> getDevicesByStatus(DeviceStatus status);

    List<DeviceDTO> getDevicesByType(DeviceType deviceType);

    List<DeviceDTO> getDevicesByLocationId(String locationId);

    void deleteDevice(String deviceId);

    // Soft delete
    void deactivateDevice(String deviceId);
}
