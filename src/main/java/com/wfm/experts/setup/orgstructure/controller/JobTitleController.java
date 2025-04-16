package com.wfm.experts.setup.orgstructure.controller;

import com.wfm.experts.setup.orgstructure.dto.JobTitleDto;
import com.wfm.experts.setup.orgstructure.service.JobTitleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/setup/org-structure/job-titles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class JobTitleController {

    private final JobTitleService service;

    @PostMapping
    public JobTitleDto create(@RequestBody JobTitleDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public JobTitleDto update(@PathVariable Long id, @RequestBody JobTitleDto dto) {
        return service.update(id, dto);
    }

    @GetMapping("/{id}")
    public JobTitleDto getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping
    public List<JobTitleDto> getAll() {
        return service.getAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
