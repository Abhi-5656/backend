package com.wfm.experts.setup.wfm.leavepolicy.service.impl;

import com.wfm.experts.setup.wfm.leavepolicy.dto.LeavePolicySettingDto;
import com.wfm.experts.setup.wfm.leavepolicy.dto.LeaveProfileDto;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeaveProfile;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeaveProfilePolicy;
import com.wfm.experts.setup.wfm.leavepolicy.exception.LeaveProfileAlreadyExistsException;
import com.wfm.experts.setup.wfm.leavepolicy.exception.LeaveProfileNotFoundException;
import com.wfm.experts.setup.wfm.leavepolicy.exception.ResourceNotFoundException;
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

        LeaveProfile leaveProfile = new LeaveProfile();
        leaveProfile.setProfileName(dto.getProfileName());

        Set<LeaveProfilePolicy> profilePolicies = new HashSet<>();
        for (LeavePolicySettingDto settingDto : dto.getLeavePolicySettings()) {
            LeavePolicy leavePolicy = leavePolicyRepository.findById(settingDto.getPolicyId())
                    .orElseThrow(() -> new ResourceNotFoundException("LeavePolicy not found with id: " + settingDto.getPolicyId()));

            LeaveProfilePolicy profilePolicy = new LeaveProfilePolicy();
            profilePolicy.setLeaveProfile(leaveProfile);
            profilePolicy.setLeavePolicy(leavePolicy);
            profilePolicy.setVisibility(settingDto.getVisibility());

            profilePolicies.add(profilePolicy);
        }
        leaveProfile.setLeaveProfilePolicies(profilePolicies);

        LeaveProfile savedProfile = leaveProfileRepository.save(leaveProfile);
        return leaveProfileMapper.toDto(savedProfile);
    }

    @Override
    public LeaveProfileDto updateLeaveProfile(Long id, LeaveProfileDto dto) {
        LeaveProfile existingProfile = leaveProfileRepository.findById(id)
                .orElseThrow(() -> new LeaveProfileNotFoundException(id));

        leaveProfileRepository.findByProfileName(dto.getProfileName()).ifPresent(p -> {
            if (!p.getId().equals(id)) {
                throw new LeaveProfileAlreadyExistsException(dto.getProfileName());
            }
        });

        existingProfile.setProfileName(dto.getProfileName());

        // This clears the existing set and the orphanRemoval=true on the entity will delete the old records
        existingProfile.getLeaveProfilePolicies().clear();

        for (LeavePolicySettingDto settingDto : dto.getLeavePolicySettings()) {
            LeavePolicy leavePolicy = leavePolicyRepository.findById(settingDto.getPolicyId())
                    .orElseThrow(() -> new ResourceNotFoundException("LeavePolicy not found with id: " + settingDto.getPolicyId()));

            LeaveProfilePolicy profilePolicy = new LeaveProfilePolicy();
            profilePolicy.setLeaveProfile(existingProfile);
            profilePolicy.setLeavePolicy(leavePolicy);
            profilePolicy.setVisibility(settingDto.getVisibility());

            existingProfile.getLeaveProfilePolicies().add(profilePolicy);
        }

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