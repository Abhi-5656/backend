package com.wfm.experts.setup.orgstructure.service;

import com.wfm.experts.setup.orgstructure.dto.BusinessUnitDto;

import java.util.List;

public interface BusinessUnitService {
    BusinessUnitDto create(BusinessUnitDto dto);
    BusinessUnitDto update(Long id, BusinessUnitDto dto);
    void delete(Long id);
    BusinessUnitDto getById(Long id);
    List<BusinessUnitDto> getAll();
}
