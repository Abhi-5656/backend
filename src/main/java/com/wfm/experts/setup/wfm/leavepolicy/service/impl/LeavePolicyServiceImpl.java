package com.wfm.experts.setup.wfm.leavepolicy.service.impl;

import com.wfm.experts.setup.wfm.leavepolicy.dto.LeavePolicyDto;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
import com.wfm.experts.setup.wfm.leavepolicy.mapper.LeavePolicyMapper;
import com.wfm.experts.setup.wfm.leavepolicy.repository.LeavePolicyRepository;
import com.wfm.experts.setup.wfm.leavepolicy.service.LeavePolicyService;
// Assume these custom exceptions exist in your project's exception package
// import com.wfm.experts.setup.wfm.exception.DuplicateResourceException;
// import com.wfm.experts.setup.wfm.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the LeavePolicyService interface.
 * Handles the business logic for leave policy operations.
 */
@Service
@RequiredArgsConstructor // Lombok annotation for constructor injection
@Transactional // Ensures all methods are transactional
public class LeavePolicyServiceImpl implements LeavePolicyService {

    private final LeavePolicyRepository leavePolicyRepository;
    private final LeavePolicyMapper leavePolicyMapper;

    @Override
    public LeavePolicyDto createLeavePolicy(LeavePolicyDto leavePolicyDto) {
        // --- Business Rule: Check for duplicates before creating ---
        if (leavePolicyRepository.existsByPolicyName(leavePolicyDto.getPolicyName())) {
            // throw new DuplicateResourceException("Leave policy with name '" + leavePolicyDto.getPolicyName() + "' already exists.");
        }
        if (leavePolicyDto.getLeaveCode() != null && leavePolicyRepository.existsByLeaveCode(leavePolicyDto.getLeaveCode())) {
            // throw new DuplicateResourceException("Leave policy with code '" + leavePolicyDto.getLeaveCode() + "' already exists.");
        }

        LeavePolicy leavePolicy = leavePolicyMapper.toEntity(leavePolicyDto);
        LeavePolicy savedPolicy = leavePolicyRepository.save(leavePolicy);
        return leavePolicyMapper.toDto(savedPolicy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeavePolicyDto> getAllLeavePolicies() {
        return leavePolicyRepository.findAll()
                .stream()
                .map(leavePolicyMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LeavePolicyDto> getLeavePolicyById(Long id) {
        return leavePolicyRepository.findById(id)
                .map(leavePolicyMapper::toDto);
    }

    @Override
    public LeavePolicyDto updateLeavePolicy(Long id, LeavePolicyDto leavePolicyDto) {
        // --- Find the existing entity or throw an exception ---
        LeavePolicy existingPolicy = leavePolicyRepository.findById(id)
                .orElseThrow(/*() -> new ResourceNotFoundException("Leave Policy not found with id: " + id)*/);

        // --- Business Rule: Check for duplicate name/code on other policies ---
        leavePolicyRepository.findByPolicyName(leavePolicyDto.getPolicyName())
                .ifPresent(policy -> {
                    if (!policy.getId().equals(id)) {
                        // throw new DuplicateResourceException("Leave policy name '" + leavePolicyDto.getPolicyName() + "' is already in use.");
                    }
                });

        // --- Use the mapper to update the found entity from the DTO ---
        leavePolicyMapper.updateLeavePolicyFromDto(leavePolicyDto, existingPolicy);

        LeavePolicy updatedPolicy = leavePolicyRepository.save(existingPolicy);
        return leavePolicyMapper.toDto(updatedPolicy);
    }

    @Override
    public void deleteLeavePolicy(Long id) {
        if (!leavePolicyRepository.existsById(id)) {
            // throw new ResourceNotFoundException("Leave Policy not found with id: " + id);
        }
        leavePolicyRepository.deleteById(id);
    }
}