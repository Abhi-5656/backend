package com.wfm.experts.setup.wfm.requesttype.service;

import com.wfm.experts.setup.wfm.requesttype.dto.RequestTypeProfileDTO;
import java.util.List;
import java.util.Optional;

public interface RequestTypeProfileService {
    RequestTypeProfileDTO createRequestTypeProfile(RequestTypeProfileDTO dto);
    RequestTypeProfileDTO updateRequestTypeProfile(Long id, RequestTypeProfileDTO dto);
    void deleteRequestTypeProfile(Long id);
    Optional<RequestTypeProfileDTO> getRequestTypeProfileById(Long id);
    List<RequestTypeProfileDTO> getAllRequestTypeProfiles();
}