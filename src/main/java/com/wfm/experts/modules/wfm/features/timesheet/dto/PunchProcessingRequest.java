package com.wfm.experts.modules.wfm.features.timesheet.dto;

import java.io.Serializable;
import java.time.LocalDate;

public class PunchProcessingRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String employeeId;
    private LocalDate workDate;

    public PunchProcessingRequest() {
    }

    public PunchProcessingRequest(String employeeId, LocalDate workDate) {
        this.employeeId = employeeId;
        this.workDate = workDate;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    @Override
    public String toString() {
        return "PunchProcessingRequest{" +
                "employeeId='" + employeeId + '\'' +
                ", workDate=" + workDate +
                '}';
    }
}