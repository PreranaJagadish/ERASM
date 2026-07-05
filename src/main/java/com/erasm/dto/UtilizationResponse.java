package com.erasm.dto;

public class UtilizationResponse {

    private Long employeeId;
    private String employeeName;
    private Double totalAllocatedPercentage;
    private Double billablePercentage;
    private Double benchPercentage;

    public UtilizationResponse() {
    }

    public UtilizationResponse(Long employeeId, String employeeName, Double totalAllocatedPercentage,
                                Double billablePercentage, Double benchPercentage) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.totalAllocatedPercentage = totalAllocatedPercentage;
        this.billablePercentage = billablePercentage;
        this.benchPercentage = benchPercentage;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public Double getTotalAllocatedPercentage() {
        return totalAllocatedPercentage;
    }

    public void setTotalAllocatedPercentage(Double totalAllocatedPercentage) {
        this.totalAllocatedPercentage = totalAllocatedPercentage;
    }

    public Double getBillablePercentage() {
        return billablePercentage;
    }

    public void setBillablePercentage(Double billablePercentage) {
        this.billablePercentage = billablePercentage;
    }

    public Double getBenchPercentage() {
        return benchPercentage;
    }

    public void setBenchPercentage(Double benchPercentage) {
        this.benchPercentage = benchPercentage;
    }
}
