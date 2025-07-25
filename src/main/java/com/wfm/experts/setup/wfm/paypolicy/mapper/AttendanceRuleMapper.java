package com.wfm.experts.setup.wfm.paypolicy.mapper;

import com.wfm.experts.setup.wfm.paypolicy.dto.AttendanceRuleDTO;
import com.wfm.experts.setup.wfm.paypolicy.entity.AttendanceRule;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AttendanceRuleMapper {

    AttendanceRuleMapper INSTANCE = Mappers.getMapper(AttendanceRuleMapper.class);

    AttendanceRuleDTO toDto(AttendanceRule entity);

    AttendanceRule toEntity(AttendanceRuleDTO dto);
}
//package com.wfm.experts.setup.wfm.paypolicy.mapper;
//
//import com.wfm.experts.setup.wfm.paypolicy.dto.AttendanceRuleDTO;
//import com.wfm.experts.setup.wfm.paypolicy.entity.AttendanceRule;
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//import org.mapstruct.factory.Mappers;
//
//@Mapper(componentModel = "spring")
//public interface AttendanceRuleMapper {
//
//    AttendanceRuleMapper INSTANCE = Mappers.getMapper(AttendanceRuleMapper.class);
//
//    // --- Mapping from Entity to DTO ---
//    @Mapping(source = "fullDayHours", target = "unscheduledFullDayHours")
//    @Mapping(source = "fullDayMinutes", target = "unscheduledFullDayMinutes")
//    @Mapping(source = "halfDayHours", target = "unscheduledHalfDayHours")
//    @Mapping(source = "halfDayMinutes", target = "unscheduledHalfDayMinutes")
//    @Mapping(source = "fullDayPercentage", target = "scheduledFullDayPercentage")
//    @Mapping(source = "halfDayPercentage", target = "scheduledHalfDayPercentage")
//    AttendanceRuleDTO toDto(AttendanceRule entity);
//
//    // --- Mapping from DTO to Entity ---
//    @Mapping(source = "unscheduledFullDayHours", target = "fullDayHours")
//    @Mapping(source = "unscheduledFullDayMinutes", target = "fullDayMinutes")
//    @Mapping(source = "unscheduledHalfDayHours", target = "halfDayHours")
//    @Mapping(source = "unscheduledHalfDayMinutes", target = "halfDayMinutes")
//    @Mapping(source = "scheduledFullDayPercentage", target = "fullDayPercentage")
//    @Mapping(source = "scheduledHalfDayPercentage", target = "halfDayPercentage")
//    AttendanceRule toEntity(AttendanceRuleDTO dto);
//}
