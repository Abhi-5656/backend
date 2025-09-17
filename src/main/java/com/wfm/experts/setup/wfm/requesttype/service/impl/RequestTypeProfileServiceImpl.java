package com.wfm.experts.setup.wfm.requesttype.service.impl;

import com.wfm.experts.setup.wfm.requesttype.dto.RequestTypeProfileDTO;
import com.wfm.experts.setup.wfm.requesttype.entity.RequestType;
import com.wfm.experts.setup.wfm.requesttype.entity.RequestTypeProfile;
import com.wfm.experts.setup.wfm.requesttype.mapper.RequestTypeProfileMapper;
import com.wfm.experts.setup.wfm.requesttype.repository.RequestTypeProfileRepository;
import com.wfm.experts.setup.wfm.requesttype.repository.RequestTypeRepository;
import com.wfm.experts.setup.wfm.requesttype.service.RequestTypeProfileService;
import com.wfm.experts.setup.wfm.requesttype.support.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RequestTypeProfileServiceImpl implements RequestTypeProfileService {

    private final RequestTypeProfileRepository requestTypeProfileRepository;
    private final RequestTypeRepository requestTypeRepository;
    private final RequestTypeProfileMapper requestTypeProfileMapper;


    @Override
    public RequestTypeProfileDTO createRequestTypeProfile(RequestTypeProfileDTO dto) {
        if (requestTypeProfileRepository.existsByProfileName(dto.getProfileName())) {
            throw new RuntimeException("Profile with name " + dto.getProfileName() + " already exists.");
        }

        RequestTypeProfile requestTypeProfile = new RequestTypeProfile();
        requestTypeProfile.setProfileName(dto.getProfileName());

        Set<RequestType> requestTypes = new HashSet<>(requestTypeRepository.findAllById(dto.getRequestTypeIds()));
        requestTypeProfile.setRequestTypes(requestTypes);

        RequestTypeProfile savedProfile = requestTypeProfileRepository.save(requestTypeProfile);
        return requestTypeProfileMapper.toDto(savedProfile);
    }

    @Override
    public RequestTypeProfileDTO updateRequestTypeProfile(Long id, RequestTypeProfileDTO dto) {
        RequestTypeProfile existingProfile = requestTypeProfileRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("RequestTypeProfile not found with id: " + id));

        if (!existingProfile.getProfileName().equals(dto.getProfileName()) && requestTypeProfileRepository.existsByProfileName(dto.getProfileName())) {
            throw new RuntimeException("Profile with name " + dto.getProfileName() + " already exists.");
        }

        existingProfile.setProfileName(dto.getProfileName());

        Set<RequestType> requestTypes = new HashSet<>(requestTypeRepository.findAllById(dto.getRequestTypeIds()));
        existingProfile.setRequestTypes(requestTypes);

        RequestTypeProfile updatedProfile = requestTypeProfileRepository.save(existingProfile);
        return requestTypeProfileMapper.toDto(updatedProfile);
    }

    @Override
    public void deleteRequestTypeProfile(Long id) {
        if (!requestTypeProfileRepository.existsById(id)) {
            throw new NotFoundException("RequestTypeProfile not found with id: " + id);
        }
        requestTypeProfileRepository.deleteById(id);
    }

    @Override
    public Optional<RequestTypeProfileDTO> getRequestTypeProfileById(Long id) {
        return requestTypeProfileRepository.findById(id).map(requestTypeProfileMapper::toDto);
    }

    @Override
    public List<RequestTypeProfileDTO> getAllRequestTypeProfiles() {
        return requestTypeProfileRepository.findAll().stream()
                .map(requestTypeProfileMapper::toDto)
                .collect(Collectors.toList());
    }
}