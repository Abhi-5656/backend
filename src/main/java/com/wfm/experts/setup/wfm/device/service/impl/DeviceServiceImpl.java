package com.wfm.experts.setup.wfm.device.service.impl;

import com.wfm.experts.setup.wfm.device.dto.DeviceDTO;
import com.wfm.experts.setup.wfm.device.entity.Device;
import com.wfm.experts.setup.wfm.device.enums.DeviceStatus;
import com.wfm.experts.setup.wfm.device.enums.DeviceType;
import com.wfm.experts.setup.wfm.device.repository.DeviceRepository;
import com.wfm.experts.setup.wfm.device.mapper.DeviceMapper;
import com.wfm.experts.setup.wfm.device.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;

    @Override
    public DeviceDTO createDevice(DeviceDTO deviceDTO) {
        Device device = deviceMapper.toEntity(deviceDTO);
        // Set registeredOn and default fields not from UI
        device.setRegisteredOn(OffsetDateTime.now());
        device.setDeleted(Boolean.FALSE);
        device = deviceRepository.save(device);
        return deviceMapper.toDto(device);
    }

    @Override
    public DeviceDTO updateDevice(String deviceId, DeviceDTO dto) {
        Optional<Device> optionalDevice = deviceRepository.findById(deviceId);
        if (optionalDevice.isEmpty()) throw new RuntimeException("Device not found");
        Device device = optionalDevice.get();
        // Map mutable fields from DTO
        Device updatedDevice = deviceMapper.toEntity(dto);
        updatedDevice.setDeviceId(deviceId); // Ensure ID is not overwritten
        updatedDevice.setRegisteredOn(device.getRegisteredOn());
        device = deviceRepository.save(updatedDevice);
        return deviceMapper.toDto(device);
    }

    @Override
    public Optional<DeviceDTO> getDeviceById(String deviceId) {
        return deviceRepository.findById(deviceId).map(deviceMapper::toDto);
    }

    @Override
    public List<DeviceDTO> getAllDevices() {
        return deviceRepository.findAll().stream().map(deviceMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<DeviceDTO> getDevicesByStatus(DeviceStatus status) {
        return deviceRepository.findByStatus(status).stream().map(deviceMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<DeviceDTO> getDevicesByType(DeviceType deviceType) {
        return deviceRepository.findByDeviceType(deviceType).stream().map(deviceMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<DeviceDTO> getDevicesByLocationId(String locationId) {
        return deviceRepository.findByLocationId(locationId).stream().map(deviceMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public void deleteDevice(String deviceId) {
        deviceRepository.deleteById(deviceId);
    }

    @Override
    public void deactivateDevice(String deviceId) {
        deviceRepository.findById(deviceId).ifPresent(device -> {
            device.setDeleted(true);
            device.setStatus(DeviceStatus.INACTIVE);
            deviceRepository.save(device);
        });
    }
}
