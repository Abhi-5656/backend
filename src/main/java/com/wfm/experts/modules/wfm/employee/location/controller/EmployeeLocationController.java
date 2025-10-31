package com.wfm.experts.modules.wfm.employee.location.controller;

import com.wfm.experts.modules.wfm.employee.location.dto.EmployeeLocationDTO;
import com.wfm.experts.modules.wfm.employee.location.service.EmployeeLocationService;
import com.wfm.experts.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/wfm/location")
@RequiredArgsConstructor
public class EmployeeLocationController {

    private final EmployeeLocationService locationService;
    private final JwtUtil jwtUtil;

    /**
     * API endpoint for mobile devices to send employee coordinates.
     * The employeeId is extracted from the JWT token, not taken from the body,
     * ensuring that users can only submit their own location.
     *
     * @param locationDTO DTO containing latitude, longitude, and timestamp.
     * @param authorizationHeader The "Authorization" header containing the Bearer token.
     * @return A ResponseEntity indicating acceptance.
     */
    @PostMapping("/track")
    public ResponseEntity<Map<String, String>> trackEmployeeLocation(
            @Valid @RequestBody EmployeeLocationDTO locationDTO,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {

        try {
            // Extract employeeId from the JWT token
            String token = authorizationHeader.substring(7);
            String employeeId = jwtUtil.extractEmployeeId(token);

            if (employeeId == null || employeeId.isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token: Employee ID not found.");
            }

            // Set the authenticated employeeId on the DTO
            locationDTO.setEmployeeId(employeeId);

            // Send to service to be published to RabbitMQ
            locationService.trackLocation(locationDTO);

            return ResponseEntity.accepted().body(Map.of(
                    "status", "Location update queued",
                    "employeeId", employeeId
            ));

        } catch (Exception e) {
            // Catch potential JWT exceptions or other errors
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process location update", e);
        }
    }
}