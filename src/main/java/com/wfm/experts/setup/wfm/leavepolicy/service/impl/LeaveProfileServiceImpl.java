package com.wfm.experts.setup.wfm.leavepolicy.service.impl;

import com.wfm.experts.setup.wfm.leavepolicy.dto.LeaveProfileDto;
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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveProfileServiceImpl implements LeaveProfileService {

    private final LeaveProfileRepository leaveProfileRepository;
    private final LeavePolicyRepository leavePolicyRepository;
    private final LeaveProfileMapper leaveProfileMapper;

    @Override
    public LeaveProfileDto createLeaveProfile(LeaveProfileDto dto) {
        if (leaveProfileRepository.existsByProfileName(dto.getProfileName())) {
            throw new LeaveProfileAlreadyExistsException(dto.getProfileName());
        }

        LeaveProfile leaveProfile = leaveProfileMapper.toEntity(dto);
        Set<LeavePolicy> policies = new HashSet<>(leavePolicyRepository.findAllById(dto.getLeavePolicyIds()));
        leaveProfile.setLeavePolicies(policies);

        LeaveProfile savedProfile = leaveProfileRepository.save(leaveProfile);
        return leaveProfileMapper.toDto(savedProfile);
    }

    @Override
    public LeaveProfileDto updateLeaveProfile(Long id, LeaveProfileDto dto) {
        LeaveProfile existingProfile = leaveProfileRepository.findById(id)
                .orElseThrow(() -> new LeaveProfileNotFoundException(id));

        // Check if the new name is already taken by another profile
        leaveProfileRepository.findByProfileName(dto.getProfileName()).ifPresent(p -> {
            if (!p.getId().equals(id)) {
                throw new LeaveProfileAlreadyExistsException(dto.getProfileName());
            }
        });

        existingProfile.setProfileName(dto.getProfileName());
        Set<LeavePolicy> policies = new HashSet<>(leavePolicyRepository.findAllById(dto.getLeavePolicyIds()));
        existingProfile.setLeavePolicies(policies);

        LeaveProfile updatedProfile = leaveProfileRepository.save(existingProfile);
        return leaveProfileMapper.toDto(updatedProfile);
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
    public Optional<LeaveProfileDto> getLeaveProfileById(Long id) {
        return leaveProfileRepository.findById(id).map(leaveProfileMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveProfileDto> getAllLeaveProfiles() {
        return leaveProfileRepository.findAll().stream()
                .map(leaveProfileMapper::toDto)
                .collect(Collectors.toList());
    }
}