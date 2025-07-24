// src/main/java/com/wfm/experts/setup/wfm/leavepolicy/service/impl/LeaveProfileServiceImpl.java
package com.wfm.experts.setup.wfm.leavepolicy.service.impl;

import com.wfm.experts.setup.wfm.leavepolicy.dto.LeaveProfileDTO;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeaveProfile;
import com.wfm.experts.setup.wfm.leavepolicy.exception.LeaveProfileAlreadyExistsException;

import com.wfm.experts.setup.wfm.leavepolicy.exception.LeaveProfileNotFoundException;
import com.wfm.experts.setup.wfm.leavepolicy.mapper.LeaveProfileMapper;
import com.wfm.experts.setup.wfm.leavepolicy.repository.LeavePolicyRepository;
import com.wfm.experts.setup.wfm.leavepolicy.repository.LeaveProfileRepository;
import com.wfm.experts.setup.wfm.leavepolicy.service.LeaveProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveProfileServiceImpl implements LeaveProfileService {

    private final LeaveProfileRepository leaveProfileRepository;
    private final LeavePolicyRepository leavePolicyRepository;
    private final LeaveProfileMapper mapper;

    @Override
    public LeaveProfileDTO createLeaveProfile(LeaveProfileDTO dto) {
        // check for duplicate name
        if (leaveProfileRepository.existsByProfileName(dto.getProfileName())) {
            throw new LeaveProfileAlreadyExistsException(dto.getProfileName());
        }
        // map DTO → entity (policies set below)
        LeaveProfile entity = mapper.toEntity(dto);

        // lookup and attach leave policies
        Set<LeavePolicy> policies = fetchPoliciesOrThrow(dto.getLeaveIds());
        entity.setLeavePolicies(policies);

        LeaveProfile saved = leaveProfileRepository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public LeaveProfileDTO updateLeaveProfile(Long id, LeaveProfileDTO dto) {
        LeaveProfile existing = leaveProfileRepository.findById(id)
                .orElseThrow(() -> new LeaveProfileNotFoundException(id));

        // if name changed, ensure uniqueness
        if (!existing.getProfileName().equals(dto.getProfileName())
                && leaveProfileRepository.existsByProfileName(dto.getProfileName())) {
            throw new LeaveProfileAlreadyExistsException(dto.getProfileName());
        }

        existing.setProfileName(dto.getProfileName());

        // refresh policies
        Set<LeavePolicy> policies = fetchPoliciesOrThrow(dto.getLeaveIds());
        existing.getLeavePolicies().clear();
        existing.getLeavePolicies().addAll(policies);

        LeaveProfile updated = leaveProfileRepository.save(existing);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveProfileDTO getLeaveProfileById(Long id) {
        LeaveProfile entity = leaveProfileRepository.findById(id)
                .orElseThrow(() -> new LeaveProfileNotFoundException(id));
        return mapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveProfileDTO> getAllLeaveProfiles() {
        return leaveProfileRepository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteLeaveProfile(Long id) {
        if (!leaveProfileRepository.existsById(id)) {
            throw new LeaveProfileNotFoundException(id);
        }
        leaveProfileRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LeaveProfileDTO> findByProfileName(String profileName) {
        return leaveProfileRepository
                .findByProfileName(profileName)
                .map(mapper::toDto);
    }

    /**
     * Load LeavePolicy entities for the given IDs, or fail if any are missing.
     */
    private Set<LeavePolicy> fetchPoliciesOrThrow(List<Long> ids) {
        List<LeavePolicy> found = leavePolicyRepository.findAllById(ids);
        if (found.size() != ids.size()) {
            // find which IDs were not found
            Set<Long> foundIds = found.stream()
                    .map(LeavePolicy::getId)
                    .collect(Collectors.toSet());
            List<Long> missing = ids.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            throw new IllegalArgumentException("LeavePolicy IDs not found: " + missing);
        }
        return new HashSet<>(found);
    }
}
