package com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.mapper;

import com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.dto.HolidayProfileAssignmentDTO;
import com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.entity.HolidayProfileAssignment;
import com.wfm.experts.setup.wfm.holiday.entity.HolidayProfile;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface HolidayProfileAssignmentMapper {

    /** POST: DTO + one employeeId → entity */
    @Mapping(target = "id",             source = "dto.id")
    @Mapping(target = "effectiveDate",  source = "dto.effectiveDate")
    @Mapping(target = "expirationDate", source = "dto.expirationDate")
    @Mapping(target = "isActive",       source = "dto.isActive")
    @Mapping(target = "employeeId",     expression = "java(employeeId)")
    @Mapping(
            target = "holidayProfile",
            expression = "java(createHolidayProfile(dto.getHolidayProfileId()))"
    )
    HolidayProfileAssignment toEntity(HolidayProfileAssignmentDTO dto,
                                      @Context String employeeId);

    /** GET: entity → DTO */
    @Mapping(target = "id",               source = "entity.id")
    @Mapping(target = "holidayProfileId", source = "entity.holidayProfile.id")
    @Mapping(target = "effectiveDate",    source = "entity.effectiveDate")
    @Mapping(target = "expirationDate",   source = "entity.expirationDate")
    @Mapping(target = "isActive",         source = "entity.isActive")
    @Mapping(
            target     = "employeeIds",
            expression = "java(java.util.Collections.singletonList(entity.getEmployeeId()))"
    )
    HolidayProfileAssignmentDTO toDto(HolidayProfileAssignment entity);

    /**
     * Helper factory method to avoid calling a non‐existent HolidayProfile(Long) ctor.
     */
    default HolidayProfile createHolidayProfile(Long id) {
        HolidayProfile hp = new HolidayProfile();   // no-arg ctor
        hp.setId(id);
        return hp;
    }
}
