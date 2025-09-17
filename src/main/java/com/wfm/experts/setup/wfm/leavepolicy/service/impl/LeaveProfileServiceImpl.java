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

import java.util.*;
import java.util.function.Function;
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

        // Create a map of the new policies by policy ID
        Map<Long, LeavePolicySettingDto> newPoliciesMap = dto.getLeavePolicySettings().stream()
                .collect(Collectors.toMap(LeavePolicySettingDto::getPolicyId, Function.identity()));

        // Remove policies that are no longer in the new set
        existingProfile.getLeaveProfilePolicies().removeIf(
                existingPolicy -> !newPoliciesMap.containsKey(existingPolicy.getLeavePolicy().getId())
        );

        // Add or update policies
        for (LeavePolicySettingDto settingDto : dto.getLeavePolicySettings()) {
            Optional<LeaveProfilePolicy> existingPolicyOpt = existingProfile.getLeaveProfilePolicies().stream()
                    .filter(p -> p.getLeavePolicy().getId().equals(settingDto.getPolicyId()))
                    .findFirst();

            if (existingPolicyOpt.isPresent()) {
                // Update existing
                existingPolicyOpt.get().setVisibility(settingDto.getVisibility());
            } else {
                // Add new
                LeavePolicy leavePolicy = leavePolicyRepository.findById(settingDto.getPolicyId())
                        .orElseThrow(() -> new ResourceNotFoundException("LeavePolicy not found with id: " + settingDto.getPolicyId()));

                LeaveProfilePolicy newProfilePolicy = new LeaveProfilePolicy();
                newProfilePolicy.setLeaveProfile(existingProfile);
                newProfilePolicy.setLeavePolicy(leavePolicy);
                newProfilePolicy.setVisibility(settingDto.getVisibility());
                existingProfile.getLeaveProfilePolicies().add(newProfilePolicy);
            }
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