package com.wfm.experts.tenant.common.employees.dto;

import com.wfm.experts.setup.orgstructure.dto.BusinessUnitDto;
import com.wfm.experts.setup.orgstructure.dto.JobTitleDto;
import com.wfm.experts.setup.orgstructure.dto.LocationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {

    private Long id;
    private String employeeId;
    private String email;
    private String phoneNumber;
    private List<String> roles;
    private PersonalInfoDTO personalInfo;
    private OrganizationalInfoDTO organizationalInfo;
    private LocationDto workLocation;
    private BusinessUnitDto businessUnit;
    private JobTitleDto jobTitle;
    private String reportingManagerId;
    private String hrManagerId;
    private Date createdAt;
    private Date updatedAt;
}