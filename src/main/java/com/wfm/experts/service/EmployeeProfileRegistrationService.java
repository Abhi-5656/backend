package com.wfm.experts.service;





import com.wfm.experts.entity.tenant.common.dto.EmployeeProfileRegistrationDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing employee profile registrations.
 */
public interface EmployeeProfileRegistrationService {

    /**
     * Creates a new registration record or updates an existing one for an employee.
     * This method is the primary way to record that an employee has uploaded their profile image.
     *
     * @param dto A DTO containing the employee's ID, email, and the Base64 encoded image string.
     * @return The saved EmployeeProfileRegistrationDTO with updated timestamps and status.
     */
    EmployeeProfileRegistrationDTO createOrUpdateRegistration(EmployeeProfileRegistrationDTO dto);

    /**
     * Retrieves the registration record for a given employee ID.
     *
     * @param employeeId The unique ID of the employee.
     * @return An {@link Optional} containing the registration DTO if found.
     */
    Optional<EmployeeProfileRegistrationDTO> getRegistrationByEmployeeId(String employeeId);

    /**
     * Retrieves the registration record for a given email address.
     *
     * @param email The unique email of the employee.
     * @return An {@link Optional} containing the registration DTO if found.
     */
    /**
     * Creates or updates multiple employee profile registration records.
     *
     * @param dtoList A list of DTOs containing employee details and the Base64 image.
     * @return A list of the created or updated registration records.
     */
    List<EmployeeProfileRegistrationDTO> bulkCreateOrUpdateRegistrations(List<EmployeeProfileRegistrationDTO> dtoList);


    Optional<EmployeeProfileRegistrationDTO> getRegistrationByEmail(String email);

    /**
     * A quick check to see if an employee has completed their image registration.
     *
     * @param employeeId The unique ID of the employee.
     * @return {@code true} if a registration record exists and an image has been provided, {@code false} otherwise.
     */
    boolean hasRegisteredWithImage(String employeeId);

    /**
     * Deletes the registration record for a given employee ID.
     *
     * @param employeeId The unique ID of the employee whose registration record should be deleted.
     */
    void deleteRegistration(String employeeId);
}