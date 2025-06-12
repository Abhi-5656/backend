package com.wfm.experts.setup.wfm.device.repository;

import com.wfm.experts.setup.wfm.device.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {

    // Find by status
    List<Device> findByStatus(com.wfm.experts.setup.wfm.device.enums.DeviceStatus status);

    // Find all devices for a location
    List<Device> findByLocationId(String locationId);

    // Find all active devices
    List<Device> findByDeletedFalseAndStatus(com.wfm.experts.setup.wfm.device.enums.DeviceStatus status);

    // Find by device type
    List<Device> findByDeviceType(com.wfm.experts.setup.wfm.device.enums.DeviceType deviceType);

    // Optional: find all assigned to a particular user or department
    List<Device> findByAssignedTo(String assignedTo);

    // Optional: search by name (case-insensitive)
    List<Device> findByDeviceNameIgnoreCaseContaining(String deviceName);
}
