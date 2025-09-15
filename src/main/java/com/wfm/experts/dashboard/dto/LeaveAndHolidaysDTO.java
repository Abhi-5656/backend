// Create a new file: harshwfm/wfm-backend/src/main/java/com/wfm/experts/dashboard/dto/LeaveAndHolidaysDTO.java
package com.wfm.experts.dashboard.dto;

import com.wfm.experts.setup.wfm.holiday.dto.HolidayDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LeaveAndHolidaysDTO {
    private List<LeaveBalanceSummaryDTO> leaveBalances;
    private List<HolidayDTO> upcomingHolidays;
}