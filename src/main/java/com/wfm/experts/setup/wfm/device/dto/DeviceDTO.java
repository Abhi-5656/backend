package com.wfm.experts.setup.wfm.device.dto;

import com.wfm.experts.setup.wfm.device.enums.DeviceType;
import com.wfm.experts.setup.wfm.device.enums.DeviceStatus;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceDTO {
    private String deviceId;
    private String deviceName;
    private DeviceType deviceType;
    private String serialNumber;
    private String osVersion;
    private String firmwareVersion;
    private String locationId;
    private String assignedTo;
    private DeviceStatus status;
    private String geoFenceId;
    private String ipAddress;
    private String remarks;
    private List<String> allowedEmployeeIds;
    private String devicePhotoUrl;
    private OffsetDateTime registeredOn;
    private OffsetDateTime lastHeartbeat;
    private OffsetDateTime lastPunchTime;
    private Boolean deleted;
}
