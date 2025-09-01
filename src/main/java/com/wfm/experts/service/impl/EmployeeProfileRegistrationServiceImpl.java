package com.wfm.experts.service.impl;

import com.wfm.experts.tenant.common.employees.entity.EmployeeProfileRegistration;

import com.wfm.experts.tenant.common.employees.dto.EmployeeProfileRegistrationDTO;
import com.wfm.experts.tenant.common.employees.exception.ProfileRegistrationNotFoundException;
import com.wfm.experts.tenant.common.employees.exception.ResourceNotFoundException;
import com.wfm.experts.tenant.common.employees.mapper.EmployeeProfileRegistrationMapper;
import com.wfm.experts.tenant.common.employees.repository.EmployeeProfileRegistrationRepository;
import com.wfm.experts.tenant.common.employees.repository.EmployeeRepository;
import com.wfm.experts.service.EmployeeProfileRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for managing EmployeeProfileRegistration.
 * This version uses employeeId as the sole identifier.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeProfileRegistrationServiceImpl implements EmployeeProfileRegistrationService {

    private final EmployeeProfileRegistrationRepository registrationRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeProfileRegistrationMapper mapper;

    /**
     * Creates a new registration or updates an existing one for an employee.
     * It validates that the employee exists before creating or updating the record.
     */
    @Override
    public EmployeeProfileRegistrationDTO createOrUpdateRegistration(EmployeeProfileRegistrationDTO dto) {
        // Step 1: Validate that the employee exists in the main `employees` table.
        employeeRepository.findByEmployeeId(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Cannot create profile registration: Employee not found with ID: " + dto.getEmployeeId()));

        // Step 2: Find an existing registration or create a new one.
        EmployeeProfileRegistration registration = registrationRepository.findByEmployeeId(dto.getEmployeeId())
                .orElse(new EmployeeProfileRegistration());

        // Step 3: Map data from DTO to the entity.
        registration.setEmployeeId(dto.getEmployeeId());
        registration.setEmployeeImageData(dto.getEmployeeImageData());

        // The entity's @PrePersist/@PreUpdate methods will handle the rest.
        EmployeeProfileRegistration savedEntity = registrationRepository.save(registration);
        return mapper.toDto(savedEntity);
    }

    /**
     * Retrieves the registration record for a given employee ID.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<EmployeeProfileRegistrationDTO> getRegistrationByEmployeeId(String employeeId) {
        return registrationRepository.findByEmployeeId(employeeId)
                .map(mapper::toDto);
    }

    @Override
    public List<EmployeeProfileRegistrationDTO> bulkCreateOrUpdateRegistrations(List<EmployeeProfileRegistrationDTO> dtoList) {
        return dtoList.stream()
                .map(this::createOrUpdateRegistration)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeProfileRegistrationDTO> getAllRegistrations() {
        return registrationRepository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }


    /**
     * This method is no longer needed as the email has been removed.
     * It is kept here for reference but can be removed from the service interface.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<EmployeeProfileRegistrationDTO> getRegistrationByEmail(String email) {
        // Since email is removed, this would now be an invalid operation.
        // You can either remove this method or have it return empty.
        return Optional.empty();
    }


    /**
     * Checks if an employee has registered with an image.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean hasRegisteredWithImage(String employeeId) {
        return registrationRepository.findByEmployeeId(employeeId)
                .map(EmployeeProfileRegistration::isHasRegisteredWithImage)
                .orElse(false);
    }

    /**
     * Deletes a registration record by employee ID.
     */
    @Override
    public void deleteRegistration(String employeeId) {
        EmployeeProfileRegistration registration = registrationRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ProfileRegistrationNotFoundException("Cannot delete: No registration record found for employee ID: " + employeeId));
        registrationRepository.delete(registration);
    }
}