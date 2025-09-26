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
import java.util.Objects;
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
        Objects.requireNonNull(dto, "RequestTypeProfileDTO cannot be null");
        if (dto.getProfileName() == null || dto.getProfileName().isBlank()) {
            throw new IllegalArgumentException("profileName is required");
        }
        if (requestTypeProfileRepository.existsByProfileName(dto.getProfileName())) {
            throw new RuntimeException("Profile with name " + dto.getProfileName() + " already exists.");
        }

        // Map simple fields (now only id/name + relationships)
        RequestTypeProfile profile = requestTypeProfileMapper.toEntity(dto);

        // Resolve request types from IDs
        Set<Long> ids = dto.getRequestTypeIds() == null ? Set.of() : new HashSet<>(dto.getRequestTypeIds());
        if (!ids.isEmpty()) {
            List<RequestType> found = requestTypeRepository.findAllById(ids);
            if (found.size() != ids.size()) {
                // find which are missing to fail early & noisily
                Set<Long> foundIds = found.stream().map(RequestType::getId).collect(Collectors.toSet());
                Set<Long> missing = ids.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toSet());
                throw new NotFoundException("Some request types not found: " + missing);
            }
            profile.setRequestTypes(new HashSet<>(found));
        } else {
            profile.setRequestTypes(new HashSet<>());
        }

        RequestTypeProfile saved = requestTypeProfileRepository.save(profile);
        return requestTypeProfileMapper.toDto(saved);
    }

    @Override
    public RequestTypeProfileDTO updateRequestTypeProfile(Long id, RequestTypeProfileDTO dto) {
        Objects.requireNonNull(id, "id cannot be null");
        Objects.requireNonNull(dto, "RequestTypeProfileDTO cannot be null");

        RequestTypeProfile existing = requestTypeProfileRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("RequestTypeProfile not found with id: " + id));

        if (dto.getProfileName() == null || dto.getProfileName().isBlank()) {
            throw new IllegalArgumentException("profileName is required");
        }

        if (!existing.getProfileName().equals(dto.getProfileName())
                && requestTypeProfileRepository.existsByProfileName(dto.getProfileName())) {
            throw new RuntimeException("Profile with name " + dto.getProfileName() + " already exists.");
        }

        // Update simple fields (dates removed)
        existing.setProfileName(dto.getProfileName());

        // Update request types
        Set<Long> ids = dto.getRequestTypeIds() == null ? Set.of() : new HashSet<>(dto.getRequestTypeIds());
        if (!ids.isEmpty()) {
            List<RequestType> found = requestTypeRepository.findAllById(ids);
            if (found.size() != ids.size()) {
                Set<Long> foundIds = found.stream().map(RequestType::getId).collect(Collectors.toSet());
                Set<Long> missing = ids.stream().filter(x -> !foundIds.contains(x)).collect(Collectors.toSet());
                throw new NotFoundException("Some request types not found: " + missing);
            }
            existing.setRequestTypes(new HashSet<>(found));
        } else {
            existing.setRequestTypes(new HashSet<>());
        }

        RequestTypeProfile updated = requestTypeProfileRepository.save(existing);
        return requestTypeProfileMapper.toDto(updated);
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
