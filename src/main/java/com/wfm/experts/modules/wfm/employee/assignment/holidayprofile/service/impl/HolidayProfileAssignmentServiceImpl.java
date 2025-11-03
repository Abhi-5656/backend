package com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.service.impl;

import com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.dto.HolidayProfileAssignmentDTO;
import com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.entity.HolidayProfileAssignment;
// Import new exceptions
import com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.exception.DuplicateHolidayProfileAssignmentException;
import com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.exception.HolidayProfileAssignmentResourceNotFoundException;
import com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.exception.HolidayProfileAssignmentValidationException;
import com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.mapper.HolidayProfileAssignmentMapper;
import com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.repository.HolidayProfileAssignmentRepository;
import com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.service.HolidayProfileAssignmentService;
import com.wfm.experts.setup.wfm.holiday.dto.HolidayDTO;
import com.wfm.experts.setup.wfm.holiday.mapper.HolidayMapper;
// Import new repositories
import com.wfm.experts.setup.wfm.holiday.repository.HolidayProfileRepository;
import com.wfm.experts.tenant.common.employees.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HolidayProfileAssignmentServiceImpl implements HolidayProfileAssignmentService {

    private final HolidayProfileAssignmentRepository repository;
    private final HolidayProfileAssignmentMapper mapper;
    private final HolidayMapper holidayMapper;

    // Add repositories needed for validation
    private final EmployeeRepository employeeRepository;
    private final HolidayProfileRepository holidayProfileRepository;

    // Define a PostgreSQL-compatible "far future" date
    private static final LocalDate POSTGRES_MAX_DATE = LocalDate.of(9999, 12, 31);

    @Override
    public List<HolidayProfileAssignmentDTO> assignHolidayProfiles(HolidayProfileAssignmentDTO dto) {

        // 1. Validate Expiration Date
        if (dto.getExpirationDate() != null && dto.getExpirationDate().isBefore(dto.getEffectiveDate())) {
            throw new HolidayProfileAssignmentValidationException("Expiration date cannot be before the effective date.");
        }

        // 2. Validate Holiday Profile exists
        if (!holidayProfileRepository.existsById(dto.getHolidayProfileId())) {
            throw new HolidayProfileAssignmentResourceNotFoundException("HolidayProfile not found with id: " + dto.getHolidayProfileId());
        }

        return dto.getEmployeeIds().stream()
                .map(empId -> {
                    // 3. Validate Employee exists
                    if (!employeeRepository.existsByEmployeeId(empId)) {
                        throw new HolidayProfileAssignmentResourceNotFoundException("Employee not found for id: " + empId);
                    }

                    // 4. Check for overlapping assignments
                    LocalDate endDate = (dto.getExpirationDate() != null) ? dto.getExpirationDate() : POSTGRES_MAX_DATE;
                    if (repository.existsOverlappingAssignment(empId, dto.getEffectiveDate(), endDate)) {
                        throw new DuplicateHolidayProfileAssignmentException("An assignment for employee " + empId + " already exists within the specified date range.");
                    }

                    // 5. Deactivate existing active assignments for this employee
                    repository.findByEmployeeId(empId).stream()
                            .filter(HolidayProfileAssignment::getIsActive)
                            .forEach(a -> {
                                a.setIsActive(false);
                                repository.save(a);
                            });

                    // 6. Create new assignment
                    HolidayProfileAssignment entity = mapper.toEntity(dto, empId);
                    entity.setIsActive(true); // Ensure new one is active
                    return repository.save(entity);
                })
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<HolidayProfileAssignmentDTO> getAllAssignments() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<HolidayProfileAssignmentDTO> getAssignmentsByEmployeeId(String employeeId) {
        return repository.findByEmployeeId(employeeId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public HolidayProfileAssignmentDTO getAssignmentById(Long id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new HolidayProfileAssignmentResourceNotFoundException(
                        "HolidayProfileAssignment not found for id: " + id));
    }

    @Override
    public void deactivateAssignment(Long id) {
        HolidayProfileAssignment entity = repository.findById(id)
                .orElseThrow(() -> new HolidayProfileAssignmentResourceNotFoundException(
                        "HolidayProfileAssignment not found for id: " + id));
        entity.setIsActive(false);
        repository.save(entity);
    }

    @Override
    public HolidayProfileAssignmentDTO expireAssignment(Long id, LocalDate expirationDate) {
        HolidayProfileAssignment entity = repository.findById(id)
                .orElseThrow(() -> new HolidayProfileAssignmentResourceNotFoundException(
                        "HolidayProfileAssignment not found for id: " + id));

        if (expirationDate.isBefore(entity.getEffectiveDate())) {
            throw new HolidayProfileAssignmentValidationException("Expiration date cannot be before the effective date.");
        }

        entity.setExpirationDate(expirationDate);
        entity.setIsActive(false); // Expiring an assignment should also deactivate it
        HolidayProfileAssignment saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public List<HolidayDTO> getAssignedHolidaysByEmployeeId(String employeeId) {
        List<HolidayProfileAssignment> assignments = repository.findByEmployeeId(employeeId);
        if (assignments.isEmpty()) {
            return Collections.emptyList();
        }

        return assignments.stream()
                .filter(a -> a.getIsActive() && a.getHolidayProfile() != null)
                .flatMap(a -> a.getHolidayProfile().getHolidays().stream())
                .distinct()
                .map(holidayMapper::toDto)
                .collect(Collectors.toList());
    }
}