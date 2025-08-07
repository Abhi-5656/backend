package com.wfm.experts.controller;




import com.wfm.experts.entity.tenant.common.dto.EmployeeProfileRegistrationDTO;
import com.wfm.experts.entity.tenant.common.exception.ProfileRegistrationNotFoundException;
import com.wfm.experts.service.EmployeeProfileRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for managing employee profile registrations.
 */
@RestController
@RequestMapping("/api/employee-profile-registrations")
@RequiredArgsConstructor
public class EmployeeProfileRegistrationController {

    private final EmployeeProfileRegistrationService registrationService;

    /**
     * Creates or updates an employee's profile registration record.
     * This is the endpoint to use when an employee uploads their profile image.
     *
     * @param dto The DTO containing employee details and the Base64 image.
     * @return A DTO of the created or updated registration record.
     */
    @PostMapping
    public ResponseEntity<EmployeeProfileRegistrationDTO> createOrUpdateRegistration(@Valid @RequestBody EmployeeProfileRegistrationDTO dto) {
        EmployeeProfileRegistrationDTO result = registrationService.createOrUpdateRegistration(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Retrieves a registration record by the employee's ID.
     *
     * @param employeeId The unique ID of the employee.
     * @return The registration record if found.
     */

    /**
     * Creates or updates a batch of employee profile registration records.
     *
     * @param dtoList A list of DTOs for bulk processing.
     * @return A list of DTOs of the created or updated registration records.
     */
    @PostMapping("/bulk")
    public ResponseEntity<List<EmployeeProfileRegistrationDTO>> bulkCreateOrUpdateRegistrations(@Valid @RequestBody List<EmployeeProfileRegistrationDTO> dtoList) {
        List<EmployeeProfileRegistrationDTO> results = registrationService.bulkCreateOrUpdateRegistrations(dtoList);
        return ResponseEntity.status(HttpStatus.CREATED).body(results);
    }

    @GetMapping("/by-employee-id/{employeeId}")
    public ResponseEntity<EmployeeProfileRegistrationDTO> getRegistrationByEmployeeId(@PathVariable String employeeId) {
        return registrationService.getRegistrationByEmployeeId(employeeId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ProfileRegistrationNotFoundException("No registration record found for employee ID: " + employeeId));
    }

    /**
     * Checks if an employee has completed their registration by providing an image.
     *
     * @param employeeId The unique ID of the employee.
     * @return A JSON object indicating the registration status.
     */
    @GetMapping("/status/{employeeId}")
    public ResponseEntity<Map<String, Boolean>> getRegistrationStatus(@PathVariable String employeeId) {
        boolean hasRegistered = registrationService.hasRegisteredWithImage(employeeId);
        return ResponseEntity.ok(Map.of("hasRegisteredWithImage", hasRegistered));
    }

    /**
     * Deletes a registration record for a given employee ID.
     *
     * @param employeeId The employee ID whose registration record should be removed.
     * @return A success message indicating the deletion was successful.
     */
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<Map<String, String>> deleteRegistration(@PathVariable String employeeId) {
        registrationService.deleteRegistration(employeeId);
        return ResponseEntity.ok(Map.of("message", "Registration record for employee " + employeeId + " was successfully deleted."));
    }
}