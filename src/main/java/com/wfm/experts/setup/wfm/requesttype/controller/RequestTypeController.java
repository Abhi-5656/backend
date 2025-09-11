package com.wfm.experts.setup.wfm.requesttype.controller;

import com.wfm.experts.setup.wfm.requesttype.dto.RequestTypeDTO;
import com.wfm.experts.setup.wfm.requesttype.service.RequestTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/setup/wfm/request-types")
@RequiredArgsConstructor
@Validated
public class RequestTypeController {

    private final RequestTypeService requestTypeService;

    /**
     * Create a new Request Type with nested configs.
     */
    @PostMapping
    public ResponseEntity<RequestTypeDTO> create(@Valid @RequestBody RequestTypeDTO body) {
        RequestTypeDTO created = requestTypeService.create(body);
        return ResponseEntity.ok(created);
    }

    /**
     * Get by id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RequestTypeDTO> get(@PathVariable Long id) {
        Optional<RequestTypeDTO> dto = requestTypeService.findById(id);
        return dto.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Paged list with optional filters.
     */
    @GetMapping
    public ResponseEntity<Page<RequestTypeDTO>> list(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveTo,
            @RequestParam(required = false, name = "activeOn")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate activeOnDate,
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        Page<RequestTypeDTO> page = requestTypeService.list(effectiveFrom, effectiveTo, activeOnDate, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Full update (PUT semantics).
     */
    @PutMapping("/{id}")
    public ResponseEntity<RequestTypeDTO> update(@PathVariable Long id,
                                                 @Valid @RequestBody RequestTypeDTO body) {
        RequestTypeDTO updated = requestTypeService.update(id, body);
        return ResponseEntity.ok(updated);
    }

    /**
     * Partial update (PATCH semantics).
     */
    @PatchMapping("/{id}")
    public ResponseEntity<RequestTypeDTO> patch(@PathVariable Long id,
                                                @RequestBody RequestTypeDTO body) {
        RequestTypeDTO patched = requestTypeService.patch(id, body);
        return ResponseEntity.ok(patched);
    }

    /**
     * Hard delete.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        requestTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Check existence (lightweight).
     */
    @GetMapping("/{id}/exists")
    public ResponseEntity<Void> exists(@PathVariable Long id) {
        return requestTypeService.exists(id) ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
