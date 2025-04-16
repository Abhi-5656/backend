package com.wfm.experts.setup.orgstructure.controller;

import com.wfm.experts.setup.orgstructure.dto.LocationDto;
import com.wfm.experts.setup.orgstructure.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/setup/org-structure/job-title-assignment")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class JobTitleAssignmentController {

    private final LocationService locationService;

    /**
     * 🔗 Assign job titles to a location
     */
    @PostMapping("/assign")
    public ResponseEntity<LocationDto> assignJobTitle(
            @RequestParam("locationId") Long locationId,
            @RequestParam("jobTitleId") Long jobTitleId) {
        return ResponseEntity.ok(locationService.assignJobTitle(locationId, jobTitleId));
    }

    @PostMapping("/remove")
    public ResponseEntity<LocationDto> removeJobTitle(
            @RequestParam("locationId") Long locationId,
            @RequestParam("jobTitleId") Long jobTitleId) {
        return ResponseEntity.ok(locationService.removeJobTitle(locationId, jobTitleId));
    }
}
