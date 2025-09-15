package com.wfm.experts.service.impl;

import ai.djl.MalformedModelException;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.translate.TranslateException;
import com.wfm.experts.service.FaceRecognitionService;
import com.wfm.experts.tenant.common.employees.entity.EmployeeProfileRegistration;
import com.wfm.experts.tenant.common.employees.dto.EmployeeProfileRegistrationDTO;
import com.wfm.experts.tenant.common.employees.exception.ProfileRegistrationNotFoundException;
import com.wfm.experts.tenant.common.employees.exception.ResourceNotFoundException;
import com.wfm.experts.tenant.common.employees.mapper.EmployeeProfileRegistrationMapper;
import com.wfm.experts.tenant.common.employees.repository.EmployeeProfileRegistrationRepository;
import com.wfm.experts.tenant.common.employees.repository.EmployeeRepository;
import com.wfm.experts.service.EmployeeProfileRegistrationService;
import com.wfm.experts.util.EmbeddingConverter; // Import the converter
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeProfileRegistrationServiceImpl implements EmployeeProfileRegistrationService {

    private final EmployeeProfileRegistrationRepository registrationRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeProfileRegistrationMapper mapper;
    private final FaceRecognitionService faceRecognitionService;

    @Override
    public EmployeeProfileRegistrationDTO createOrUpdateRegistration(EmployeeProfileRegistrationDTO dto) {
        employeeRepository.findByEmployeeId(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Cannot create profile registration: Employee not found with ID: " + dto.getEmployeeId()));

        EmployeeProfileRegistration registration = registrationRepository.findByEmployeeId(dto.getEmployeeId())
                .orElse(new EmployeeProfileRegistration());

        registration.setEmployeeId(dto.getEmployeeId());
        registration.setEmployeeImageData(dto.getEmployeeImageData());

        try {
            float[] faceEmbedding = faceRecognitionService.getFaceEmbedding(dto.getEmployeeImageData());
            registration.setFaceEmbedding(EmbeddingConverter.toByteArray(faceEmbedding)); // Convert to byte[]
        } catch (IOException | TranslateException | ModelNotFoundException | MalformedModelException e) {
            throw new RuntimeException("Failed to generate face embedding: " + e.getMessage(), e);
        }

        EmployeeProfileRegistration savedEntity = registrationRepository.save(registration);
        return mapper.toDto(savedEntity);
    }

    // ... other methods in the class remain the same
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


    @Override
    @Transactional(readOnly = true)
    public Optional<EmployeeProfileRegistrationDTO> getRegistrationByEmail(String email) {
        return Optional.empty();
    }


    @Override
    @Transactional(readOnly = true)
    public boolean hasRegisteredWithImage(String employeeId) {
        return registrationRepository.findByEmployeeId(employeeId)
                .map(EmployeeProfileRegistration::isHasRegisteredWithImage)
                .orElse(false);
    }

    @Override
    public void deleteRegistration(String employeeId) {
        EmployeeProfileRegistration registration = registrationRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ProfileRegistrationNotFoundException("Cannot delete: No registration record found for employee ID: " + employeeId));
        registrationRepository.delete(registration);
    }
}