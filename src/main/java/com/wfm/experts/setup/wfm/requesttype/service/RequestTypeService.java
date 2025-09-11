package com.wfm.experts.setup.wfm.requesttype.service;

import com.wfm.experts.setup.wfm.requesttype.dto.RequestTypeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

public interface RequestTypeService {

    /**
     * Create a new Request Type with nested configs.
     * Expected: dto.id == null; nested config ids ignored.
     */
    RequestTypeDTO create(RequestTypeDTO dto);

    /**
     * Get one by id.
     */
    Optional<RequestTypeDTO> findById(Long id);

    /**
     * Paged list. Optional filters allow simple server-side querying.
     * If filters are null, returns all.
     *
     * @param effectiveFrom inclusive lower bound for effectiveDate (nullable)
     * @param effectiveTo   inclusive upper bound for effectiveDate (nullable)
     * @param activeOnDate  return records whose effective/expiration window contains this date (nullable)
     * @param pageable      page+sort definition
     */
    Page<RequestTypeDTO> list(LocalDate effectiveFrom,
                              LocalDate effectiveTo,
                              LocalDate activeOnDate,
                              Pageable pageable);

    /**
     * Full update (PUT semantics): replaces values; missing nested sections should still be provided.
     * Expected: dto.id == id (or null and will be set).
     */
    RequestTypeDTO update(Long id, RequestTypeDTO dto);

    /**
     * Partial update (PATCH semantics): only non-null fields in dto are applied.
     * Nested DTOs: non-null child fields are applied; nulls are ignored.
     */
    RequestTypeDTO patch(Long id, RequestTypeDTO dto);

    /**
     * Hard delete the aggregate and its owned configs.
     */
    void delete(Long id);

    /**
     * Existence/validation helper.
     */
    boolean exists(Long id);
}
