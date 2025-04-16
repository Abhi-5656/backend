package com.wfm.experts.setup.orgstructure.controller;

import com.wfm.experts.setup.orgstructure.dto.BusinessUnitDto;
import com.wfm.experts.setup.orgstructure.service.BusinessUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/setup/org-structure/business-units")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class BusinessUnitController {

    private final BusinessUnitService service;

    @PostMapping
    public BusinessUnitDto create(@RequestBody BusinessUnitDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public BusinessUnitDto update(@PathVariable Long id, @RequestBody BusinessUnitDto dto) {
        return service.update(id, dto);
    }

    @GetMapping("/{id}")
    public BusinessUnitDto getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping
    public List<BusinessUnitDto> getAll() {
        return service.getAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
