package com.wfm.experts.setup.wfm.device.controller;

import com.wfm.experts.setup.wfm.controller.WfmSetupController;
import com.wfm.experts.setup.wfm.device.dto.DeviceDTO;
import com.wfm.experts.setup.wfm.device.enums.DeviceStatus;
import com.wfm.experts.setup.wfm.device.enums.DeviceType;
import com.wfm.experts.setup.wfm.device.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController extends WfmSetupController {

    private final DeviceService deviceService;

    @PostMapping
    public ResponseEntity<DeviceDTO> createDevice(@RequestBody DeviceDTO deviceDTO) {
        DeviceDTO created = deviceService.createDevice(deviceDTO);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{deviceId}")
    public ResponseEntity<DeviceDTO> updateDevice(@PathVariable String deviceId,
                                                  @RequestBody DeviceDTO deviceDTO) {
        DeviceDTO updated = deviceService.updateDevice(deviceId, deviceDTO);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<DeviceDTO> getDeviceById(@PathVariable String deviceId) {
        Optional<DeviceDTO> device = deviceService.getDeviceById(deviceId);
        return device.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<DeviceDTO>> getAllDevices() {
        return ResponseEntity.ok(deviceService.getAllDevices());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<DeviceDTO>> getDevicesByStatus(@PathVariable DeviceStatus status) {
        return ResponseEntity.ok(deviceService.getDevicesByStatus(status));
    }

    @GetMapping("/type/{deviceType}")
    public ResponseEntity<List<DeviceDTO>> getDevicesByType(@PathVariable DeviceType deviceType) {
        return ResponseEntity.ok(deviceService.getDevicesByType(deviceType));
    }

    @GetMapping("/location/{locationId}")
    public ResponseEntity<List<DeviceDTO>> getDevicesByLocation(@PathVariable String locationId) {
        return ResponseEntity.ok(deviceService.getDevicesByLocationId(locationId));
    }

    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Void> deleteDevice(@PathVariable String deviceId) {
        deviceService.deleteDevice(deviceId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{deviceId}/deactivate")
    public ResponseEntity<Void> deactivateDevice(@PathVariable String deviceId) {
        deviceService.deactivateDevice(deviceId);
        return ResponseEntity.noContent().build();
    }
}
