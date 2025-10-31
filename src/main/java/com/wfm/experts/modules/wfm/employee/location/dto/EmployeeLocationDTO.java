package com.wfm.experts.modules.wfm.employee.location.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO for receiving location updates from the mobile app.
 * Also used as the message body for RabbitMQ.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeLocationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The unique ID of the employee. This will be set by the server, not the client.
     */
    private String employeeId;

    /**
     * Latitude coordinate.
     */
    @NotNull(message = "Latitude is required")
    private Double latitude;

    /**
     * Longitude coordinate.
     */
    @NotNull(message = "Longitude is required")
    private Double longitude;

    /**
     * The UTC timestamp when the location was recorded by the device.
     */
    @NotNull(message = "Timestamp is required")
    private Instant timestamp;

    /**
     * Type of event associated with the location ping (e.g., "PING", "IN", "OUT").
     */
    @NotBlank(message = "Punch type is required")
    private String punchType;
}