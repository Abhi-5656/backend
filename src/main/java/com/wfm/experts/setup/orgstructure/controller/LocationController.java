package com.wfm.experts.setup.orgstructure.controller;

import com.wfm.experts.setup.orgstructure.dto.LocationDto;
import com.wfm.experts.setup.orgstructure.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/setup/org-structure/locations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class LocationController {

    private final LocationService service;

    @PostMapping
    public LocationDto create(@RequestBody LocationDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public LocationDto update(@PathVariable Long id, @RequestBody LocationDto dto) {
        return service.update(id, dto);
    }

    @GetMapping("/{id}")
    public LocationDto getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping
    public List<LocationDto> getAll() {
        return service.getAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
