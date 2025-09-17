package com.wfm.experts.setup.wfm.requesttype.controller;

import com.wfm.experts.setup.wfm.requesttype.dto.RequestTypeDTO;
import com.wfm.experts.setup.wfm.requesttype.service.RequestTypeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/setup/wfm/request-types")
public class RequestTypeController {

    private final RequestTypeService requestTypeService;

    public RequestTypeController(RequestTypeService requestTypeService) {
        this.requestTypeService = requestTypeService;
    }

    @GetMapping
    public ResponseEntity<Page<RequestTypeDTO>> getAllRequestTypes(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date effectiveDate,
            Pageable pageable) {
        return ResponseEntity.ok(requestTypeService.findAll(name, effectiveDate, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RequestTypeDTO> getRequestTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(requestTypeService.findById(id));
    }

    @PostMapping
    public ResponseEntity<RequestTypeDTO> createRequestType(@RequestBody RequestTypeDTO requestTypeDTO) {
        return new ResponseEntity<>(requestTypeService.create(requestTypeDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RequestTypeDTO> updateRequestType(@PathVariable Long id, @RequestBody RequestTypeDTO requestTypeDTO) {
        return ResponseEntity.ok(requestTypeService.update(id, requestTypeDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequestType(@PathVariable Long id) {
        requestTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}