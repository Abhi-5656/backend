package com.wfm.experts.setup.wfm.holiday.controller;

import com.wfm.experts.setup.wfm.controller.WfmSetupController;
import com.wfm.experts.setup.wfm.holiday.dto.HolidayDTO;
import com.wfm.experts.setup.wfm.holiday.service.HolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/setup/wfm/holidays")
@RequiredArgsConstructor
public class HolidayController extends WfmSetupController {

    private final HolidayService holidayService;

    @PostMapping
    public ResponseEntity<HolidayDTO> createHoliday(@RequestBody HolidayDTO holidayDTO) {
        HolidayDTO created = holidayService.createHoliday(holidayDTO);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HolidayDTO> updateHoliday(
            @PathVariable Long id,
            @RequestBody HolidayDTO holidayDTO) {
        HolidayDTO updated = holidayService.updateHoliday(id, holidayDTO);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HolidayDTO> getHoliday(@PathVariable Long id) {
        HolidayDTO holiday = holidayService.getHoliday(id);
        return ResponseEntity.ok(holiday);
    }

    @GetMapping
    public ResponseEntity<List<HolidayDTO>> getAllHolidays() {
        List<HolidayDTO> holidays = holidayService.getAllHolidays();
        return ResponseEntity.ok(holidays);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHoliday(@PathVariable Long id) {
        holidayService.deleteHoliday(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/multi_create")
    public ResponseEntity<List<HolidayDTO>> createHolidays(@RequestBody List<HolidayDTO> holidayDTOs) {
        List<HolidayDTO> createdList = holidayService.createHolidays(holidayDTOs);
        return ResponseEntity.ok(createdList);
    }
}
