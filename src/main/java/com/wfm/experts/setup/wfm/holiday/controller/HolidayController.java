
//package com.wfm.experts.setup.wfm.holiday.controller;
//
//import com.wfm.experts.setup.wfm.controller.WfmSetupController;
//import com.wfm.experts.setup.wfm.holiday.dto.HolidayDTO;
//import com.wfm.experts.setup.wfm.holiday.service.HolidayService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/setup/wfm/holidays")
//@RequiredArgsConstructor
//public class HolidayController extends WfmSetupController {
//
//    private final HolidayService holidayService;
//
//    @PostMapping
//    @PreAuthorize("hasAuthority('wfm:setup:holiday:create')")
//    public ResponseEntity<HolidayDTO> createHoliday(@RequestBody HolidayDTO holidayDTO) {
//        HolidayDTO created = holidayService.createHoliday(holidayDTO);
//        return ResponseEntity.ok(created);
//    }
//
//    @PutMapping("/{id}")
//    @PreAuthorize("hasAuthority('wfm:setup:holiday:update')")
//    public ResponseEntity<HolidayDTO> updateHoliday(
//            @PathVariable Long id,
//            @RequestBody HolidayDTO holidayDTO) {
//        HolidayDTO updated = holidayService.updateHoliday(id, holidayDTO);
//        return ResponseEntity.ok(updated);
//    }
//
//    @GetMapping("/{id}")
////    @PreAuthorize("hasAuthority('wfm:setup:holiday:read')")
//    public ResponseEntity<HolidayDTO> getHoliday(@PathVariable Long id) {
//        HolidayDTO holiday = holidayService.getHoliday(id);
//        return ResponseEntity.ok(holiday);
//    }
//
//    @GetMapping
////    @PreAuthorize("hasAuthority('wfm:setup:holiday:read')")
//    public ResponseEntity<List<HolidayDTO>> getAllHolidays() {
//        List<HolidayDTO> holidays = holidayService.getAllHolidays();
//        return ResponseEntity.ok(holidays);
//    }
//
//    @DeleteMapping("/{id}")
//    @PreAuthorize("hasAuthority('wfm:setup:holiday:delete')")
//    public ResponseEntity<Void> deleteHoliday(@PathVariable Long id) {
//        holidayService.deleteHoliday(id);
//        return ResponseEntity.noContent().build();
//    }
//
//    @PostMapping("/multi_create")
////    @PreAuthorize("hasAuthority('wfm:setup:holiday:create')")
//    public ResponseEntity<List<HolidayDTO>> createHolidays(@RequestBody List<HolidayDTO> holidayDTOs) {
//        List<HolidayDTO> createdList = holidayService.createHolidays(holidayDTOs);
//        return ResponseEntity.ok(createdList);
//    }
//}


package com.wfm.experts.setup.wfm.holiday.controller;

import com.wfm.experts.setup.wfm.controller.WfmSetupController;
import com.wfm.experts.setup.wfm.holiday.dto.HolidayDTO;
import com.wfm.experts.setup.wfm.holiday.service.HolidayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/setup/wfm/holidays")
@RequiredArgsConstructor
@Validated
public class HolidayController extends WfmSetupController {

    private final HolidayService holidayService;

    @PostMapping
    @PreAuthorize("hasAuthority('wfm:setup:holiday:create')")
    public ResponseEntity<HolidayDTO> createHoliday(@Valid @RequestBody HolidayDTO holidayDTO) {
        HolidayDTO created = holidayService.createHoliday(holidayDTO);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('wfm:setup:holiday:update')")
    public ResponseEntity<HolidayDTO> updateHoliday(
            @PathVariable Long id,
            @Valid @RequestBody HolidayDTO holidayDTO) {
        HolidayDTO updated = holidayService.updateHoliday(id, holidayDTO);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('wfm:setup:holiday:read')")
    public ResponseEntity<HolidayDTO> getHoliday(@PathVariable Long id) {
        HolidayDTO holiday = holidayService.getHoliday(id);
        return ResponseEntity.ok(holiday);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('wfm:setup:holiday:read')")
    public ResponseEntity<List<HolidayDTO>> getAllHolidays() {
        List<HolidayDTO> holidays = holidayService.getAllHolidays();
        return ResponseEntity.ok(holidays);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('wfm:setup:holiday:delete')")
    public ResponseEntity<Void> deleteHoliday(@PathVariable Long id) {
        holidayService.deleteHoliday(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/multi_create")
    @PreAuthorize("hasAuthority('wfm:setup:holiday:create')")
    public ResponseEntity<List<HolidayDTO>> createHolidays(@RequestBody List<@Valid HolidayDTO> holidayDTOs) {
        List<HolidayDTO> createdList = holidayService.createHolidays(holidayDTOs);
        return ResponseEntity.ok(createdList);
    }
}