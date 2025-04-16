package com.wfm.experts.setup.orgstructure.service;

import com.wfm.experts.setup.orgstructure.dto.LocationDto;

import java.util.List;

public interface LocationService {
    LocationDto create(LocationDto dto);
    LocationDto update(Long id, LocationDto dto);
    void delete(Long id);
    LocationDto getById(Long id);
    List<LocationDto> getAll();
}
