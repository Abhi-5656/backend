package com.wfm.experts.hr.recruitmentonboarding.application.controller;

import com.wfm.experts.hr.recruitmentonboarding.application.dto.JobApplicationDto;
import com.wfm.experts.hr.recruitmentonboarding.application.service.JobApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public/jobs")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    // ✅ Apply to a job
    @PostMapping("/{jobId}/apply")
    public ResponseEntity<JobApplicationDto> applyForJob(
            @PathVariable Long jobId,
            @RequestBody JobApplicationDto applicationDto
    ) {
        applicationDto.setJobId(jobId);
        return ResponseEntity.ok(jobApplicationService.applyForJob(applicationDto));
    }

    // ✅ Admin / HR View: List all applications for a job
    @GetMapping("/{jobId}/applications")
    public ResponseEntity<List<JobApplicationDto>> getApplicationsByJobId(
            @PathVariable Long jobId
    ) {
        return ResponseEntity.ok(jobApplicationService.getApplicationsByJobId(jobId));
    }

    // ✅ Optional: Get single application detail
    @GetMapping("/applications/{applicationId}")
    public ResponseEntity<JobApplicationDto> getApplicationById(
            @PathVariable Long applicationId
    ) {
        return ResponseEntity.ok(jobApplicationService.getApplicationById(applicationId));
    }
}
