package com.erasm.service;

import com.erasm.dto.AllocationResponse;
import com.erasm.dto.ProjectAllocationReportResponse;
import com.erasm.dto.SkillReportResponse;
import com.erasm.dto.UtilizationResponse;
import com.erasm.entity.Allocation;
import com.erasm.entity.EmployeeSkill;
import com.erasm.entity.Project;
import com.erasm.repository.AllocationRepository;
import com.erasm.repository.EmployeeSkillRepository;
import com.erasm.repository.ProjectRepository;
import com.erasm.repository.SkillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Module 10: Reports - Skill Report, Utilization Report, Project Allocation Report.
 */
@Service
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    private final SkillRepository skillRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final ProjectRepository projectRepository;
    private final AllocationRepository allocationRepository;
    private final DashboardService dashboardService;

    public ReportService(SkillRepository skillRepository, EmployeeSkillRepository employeeSkillRepository,
                          ProjectRepository projectRepository, AllocationRepository allocationRepository,
                          DashboardService dashboardService) {
        this.skillRepository = skillRepository;
        this.employeeSkillRepository = employeeSkillRepository;
        this.projectRepository = projectRepository;
        this.allocationRepository = allocationRepository;
        this.dashboardService = dashboardService;
    }

    /** Employees grouped by skill. */
    public List<SkillReportResponse> getSkillReport() {
        logger.info("Generating skill report");
        return skillRepository.findAll().stream()
                .map(skill -> {
                    List<String> employeeNames = employeeSkillRepository.findBySkill_Id(skill.getId()).stream()
                            .map(EmployeeSkill::getEmployee)
                            .map(employee -> employee.getUser().getName())
                            .toList();
                    return new SkillReportResponse(skill.getSkillName(), employeeNames);
                })
                .toList();
    }

    /** Skill report filtered down to a single skill name (case-insensitive). */
    public List<SkillReportResponse> getSkillReport(String skillNameFilter) {
        if (skillNameFilter == null || skillNameFilter.isBlank()) {
            return getSkillReport();
        }
        logger.info("Generating skill report filtered by skill name: {}", skillNameFilter);
        return getSkillReport().stream()
                .filter(r -> r.getSkillName().equalsIgnoreCase(skillNameFilter))
                .toList();
    }

    /** Employee utilization percentage (billable / bench) for every employee. */
    public List<UtilizationResponse> getUtilizationReport() {
        logger.info("Generating utilization report");
        return dashboardService.getUtilizationForAllEmployees();
    }

    /** Employees assigned to each project. */
    public List<ProjectAllocationReportResponse> getProjectAllocationReport() {
        logger.info("Generating project allocation report");
        List<Project> projects = projectRepository.findAll();
        return projects.stream()
                .map(project -> {
                    List<AllocationResponse> allocations = allocationRepository.findByProject_Id(project.getId())
                            .stream()
                            .map(this::toAllocationResponse)
                            .toList();
                    return new ProjectAllocationReportResponse(project.getId(), project.getProjectName(), allocations);
                })
                .toList();
    }

    /** Project allocation report filtered down to a single project id. */
    public List<ProjectAllocationReportResponse> getProjectAllocationReport(Long projectIdFilter) {
        if (projectIdFilter == null) {
            return getProjectAllocationReport();
        }
        logger.info("Generating project allocation report filtered by project id: {}", projectIdFilter);
        return getProjectAllocationReport().stream()
                .filter(r -> r.getProjectId().equals(projectIdFilter))
                .toList();
    }

    private AllocationResponse toAllocationResponse(Allocation allocation) {
        AllocationResponse response = new AllocationResponse();
        response.setId(allocation.getId());
        response.setEmployeeId(allocation.getEmployee().getId());
        response.setEmployeeName(allocation.getEmployee().getUser().getName());
        response.setProjectId(allocation.getProject().getId());
        response.setProjectName(allocation.getProject().getProjectName());
        response.setAllocationPercentage(allocation.getAllocationPercentage());
        response.setStartDate(allocation.getStartDate());
        response.setEndDate(allocation.getEndDate());
        response.setStatus(allocation.getStatus());
        return response;
    }
}
