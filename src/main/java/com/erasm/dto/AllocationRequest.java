package com.erasm.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class AllocationRequest {

    @NotNull(message = "Employee id is mandatory")
    private Long employeeId;

    @NotNull(message = "Project id is mandatory")
    private Long projectId;

    private Long resourceRequestId;

    @NotNull(message = "Allocation percentage is mandatory")
    @DecimalMin(value = "1.0", message = "Allocation percentage must be greater than 0")
    @DecimalMax(value = "100.0", message = "Allocation percentage cannot exceed 100")
    private Double allocationPercentage;

    private String startDate;
    private String endDate;

    public AllocationRequest() {
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getResourceRequestId() {
        return resourceRequestId;
    }

    public void setResourceRequestId(Long resourceRequestId) {
        this.resourceRequestId = resourceRequestId;
    }

    public Double getAllocationPercentage() {
        return allocationPercentage;
    }

    public void setAllocationPercentage(Double allocationPercentage) {
        this.allocationPercentage = allocationPercentage;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
