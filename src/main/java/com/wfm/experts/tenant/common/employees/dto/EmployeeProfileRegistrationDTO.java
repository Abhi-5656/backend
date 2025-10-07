package com.wfm.experts.tenant.common.employees.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for transferring employee profile registration data.
 * This version uses employeeId as the primary identifier.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeProfileRegistrationDTO {

    private Long id;

    @NotBlank(message = "Employee ID cannot be blank.")
    private String employeeId;

    /**
     * The employee's profile image data in Base64 encoded String format.
     */
    @NotBlank(message = "Employee image data cannot be blank.")
    private String employeeImageData;

    /**
     * A flag indicating if the employee has registered with an image.
     * This is typically set by the server and is read-only for the client.
     */
    private boolean hasRegisteredWithImage;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}