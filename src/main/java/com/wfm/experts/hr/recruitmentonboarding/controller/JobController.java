package com.wfm.experts.hr.recruitmentonboarding.controller;

import com.wfm.experts.hr.recruitmentonboarding.dto.JobDto;
import com.wfm.experts.hr.recruitmentonboarding.enums.ExperienceLevel;
import com.wfm.experts.hr.recruitmentonboarding.enums.JobStatus;
import com.wfm.experts.hr.recruitmentonboarding.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/jobs")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor

public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<JobDto> createJob(@Valid @RequestBody JobDto jobDto) {
//        System.out.println("Expiry received: " + jobDto.getExpiryDate()); // Should print value

        return ResponseEntity.ok(jobService.create(jobDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobDto> updateJob(@PathVariable Long id, @Valid @RequestBody JobDto jobDto) {
        return ResponseEntity.ok(jobService.update(id, jobDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobDto> getJobById(@PathVariable Long id) {
        return jobService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<JobDto>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        jobService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // 🔍 Custom filters

    @GetMapping("/status/{status}")
    public ResponseEntity<List<JobDto>> getJobsByStatus(@PathVariable JobStatus status) {
        return ResponseEntity.ok(jobService.getByStatus(status));
    }

    @GetMapping("/experience-level/{level}")
    public ResponseEntity<List<JobDto>> getJobsByExperienceLevel(@PathVariable ExperienceLevel level) {
        return ResponseEntity.ok(jobService.getByExperienceLevel(level));
    }

    @GetMapping("/search")
    public ResponseEntity<List<JobDto>> searchByTitle(@RequestParam String keyword) {
        return ResponseEntity.ok(jobService.searchByTitle(keyword));
    }
}
