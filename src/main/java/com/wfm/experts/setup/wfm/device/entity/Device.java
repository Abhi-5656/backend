package com.wfm.experts.setup.wfm.device.entity;

import com.wfm.experts.setup.wfm.device.enums.DeviceType;
import com.wfm.experts.setup.wfm.device.enums.DeviceStatus;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @Column(name = "device_id", nullable = false, length = 64)
    private String deviceId;

    @Column(name = "device_name", nullable = false, length = 128)
    private String deviceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false, length = 32)
    private DeviceType deviceType;

    @Column(name = "serial_number", length = 64)
    private String serialNumber;

    @Column(name = "os_version", length = 64)
    private String osVersion;

    @Column(name = "firmware_version", length = 64)
    private String firmwareVersion;

    @Column(name = "location_id", length = 64)
    private String locationId;

    @Column(name = "assigned_to", length = 64)
    private String assignedTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private DeviceStatus status;

    @Column(name = "geo_fence_id", length = 64)
    private String geoFenceId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "remarks", length = 255)
    private String remarks;

    @ElementCollection
    @CollectionTable(
            name = "device_allowed_employee_ids",
            joinColumns = @JoinColumn(name = "device_id")
    )
    @Column(name = "employee_id", length = 64)
    private List<String> allowedEmployeeIds;

    @Column(name = "device_photo_url", length = 255)
    private String devicePhotoUrl;

    @Column(name = "registered_on", nullable = false)
    private OffsetDateTime registeredOn;

    @Column(name = "last_heartbeat")
    private OffsetDateTime lastHeartbeat;

    @Column(name = "last_punch_time")
    private OffsetDateTime lastPunchTime;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
}
