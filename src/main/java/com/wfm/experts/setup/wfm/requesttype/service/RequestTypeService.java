package com.wfm.experts.setup.wfm.requesttype.service;

import com.wfm.experts.setup.wfm.requesttype.dto.RequestTypeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

public interface RequestTypeService {
    Page<RequestTypeDTO> findAll(String name, Date effectiveDate, Pageable pageable);
    RequestTypeDTO findById(Long id);
    RequestTypeDTO create(RequestTypeDTO requestTypeDTO);
    RequestTypeDTO update(Long id, RequestTypeDTO requestTypeDTO);
    void delete(Long id);
}