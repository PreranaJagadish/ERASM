package com.erasm.dto;

import java.util.List;

public class ProjectAllocationReportResponse {

    private Long projectId;
    private String projectName;
    private List<AllocationResponse> allocations;

    public ProjectAllocationReportResponse() {
    }

    public ProjectAllocationReportResponse(Long projectId, String projectName, List<AllocationResponse> allocations) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.allocations = allocations;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public List<AllocationResponse> getAllocations() {
        return allocations;
    }

    public void setAllocations(List<AllocationResponse> allocations) {
        this.allocations = allocations;
    }
}
