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

import java.time.LocalDate;
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

        // Validate & normalize dates
        normalizeAndValidateDates(dto);

        // Map simple fields
        RequestTypeProfile requestTypeProfile = requestTypeProfileMapper.toEntity(dto);

        // Resolve request types from IDs
        Set<RequestType> requestTypes = new HashSet<>(requestTypeRepository.findAllById(dto.getRequestTypeIds()));
        requestTypeProfile.setRequestTypes(requestTypes);

        // Explicitly set dates (mapper already did, but keep it obvious)
        requestTypeProfile.setEffectiveDate(dto.getEffectiveDate());
        requestTypeProfile.setExpirationDate(dto.getExpirationDate());

        RequestTypeProfile savedProfile = requestTypeProfileRepository.save(requestTypeProfile);
        return requestTypeProfileMapper.toDto(savedProfile);
    }

    @Override
    public RequestTypeProfileDTO updateRequestTypeProfile(Long id, RequestTypeProfileDTO dto) {
        RequestTypeProfile existingProfile = requestTypeProfileRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("RequestTypeProfile not found with id: " + id));

        if (!existingProfile.getProfileName().equals(dto.getProfileName())
                && requestTypeProfileRepository.existsByProfileName(dto.getProfileName())) {
            throw new RuntimeException("Profile with name " + dto.getProfileName() + " already exists.");
        }

        // Validate & normalize dates
        normalizeAndValidateDates(dto);

        // Update simple fields
        existingProfile.setProfileName(dto.getProfileName());
        existingProfile.setEffectiveDate(dto.getEffectiveDate());
        existingProfile.setExpirationDate(dto.getExpirationDate());

        // Update request types
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

    /** Ensures effectiveDate is present and expirationDate, if present, is not before effectiveDate. */
    private void normalizeAndValidateDates(RequestTypeProfileDTO dto) {
        if (dto.getEffectiveDate() == null) {
            // choose either strict validation or defaulting; I recommend strict
            // throw new IllegalArgumentException("effectiveDate is required");
            dto.setEffectiveDate(LocalDate.now()); // <- if you prefer defaulting
        }
        if (dto.getExpirationDate() != null && dto.getExpirationDate().isBefore(dto.getEffectiveDate())) {
            throw new IllegalArgumentException("expirationDate cannot be before effectiveDate");
        }
    }
}
