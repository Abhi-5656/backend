package com.wfm.experts.setup.wfm.requesttype.controller;

import com.wfm.experts.setup.wfm.controller.WfmSetupController;
import com.wfm.experts.setup.wfm.requesttype.dto.RequestTypeProfileDTO;
import com.wfm.experts.setup.wfm.requesttype.service.RequestTypeProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/setup/wfm/request-type-profiles")
@RequiredArgsConstructor
public class RequestTypeProfileController extends WfmSetupController {

    private final RequestTypeProfileService requestTypeProfileService;

    @PostMapping
    public ResponseEntity<RequestTypeProfileDTO> create(@Valid @RequestBody RequestTypeProfileDTO dto) {
        return new ResponseEntity<>(requestTypeProfileService.createRequestTypeProfile(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RequestTypeProfileDTO> update(@PathVariable Long id, @Valid @RequestBody RequestTypeProfileDTO dto) {
        return ResponseEntity.ok(requestTypeProfileService.updateRequestTypeProfile(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RequestTypeProfileDTO> getById(@PathVariable Long id) {
        return requestTypeProfileService.getRequestTypeProfileById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<RequestTypeProfileDTO>> getAll() {
        return ResponseEntity.ok(requestTypeProfileService.getAllRequestTypeProfiles());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        requestTypeProfileService.deleteRequestTypeProfile(id);
        return ResponseEntity.noContent().build();
    }
}