package com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.service.impl;

import com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.dto.HolidayProfileAssignmentDTO;
import com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.entity.HolidayProfileAssignment;
import com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.mapper.HolidayProfileAssignmentMapper;
import com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.repository.HolidayProfileAssignmentRepository;
import com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.service.HolidayProfileAssignmentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HolidayProfileAssignmentServiceImpl implements HolidayProfileAssignmentService {

    private final HolidayProfileAssignmentRepository repository;
    private final HolidayProfileAssignmentMapper mapper;

    @Override
    public List<HolidayProfileAssignmentDTO> assignHolidayProfiles(HolidayProfileAssignmentDTO dto) {
        return dto.getEmployeeIds().stream()
                .map(empId -> mapper.toEntity(dto, empId))
                .map(repository::save)
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
                .orElseThrow(() -> new EntityNotFoundException(
                        "HolidayProfileAssignment not found for id: " + id));
    }

    @Override
    public void deleteAssignment(Long id) {
        HolidayProfileAssignment entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "HolidayProfileAssignment not found for id: " + id));
        entity.setIsActive(false);
        repository.save(entity);
    }
}
