package com.erasm.service;

import com.erasm.dto.AllocationRequest;
import org.springframework.transaction.annotation.Transactional;
import com.erasm.dto.AllocationResponse;
import com.erasm.entity.Allocation;
import com.erasm.entity.Employee;
import com.erasm.entity.Project;
import com.erasm.entity.ResourceRequest;
import com.erasm.exception.AllocationException;
import com.erasm.repository.AllocationRepository;
import com.erasm.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Module 7: Resource Allocation.
 *
 * Validation rule (corrected): an employee's TOTAL active allocation
 * percentage across ALL projects must never exceed 100%, not just the
 * percentage being requested for a single project. Example from the spec:
 *   Project A = 60%, Project B = 40%  -> Total 100%  -> Allowed
 *   Project A = 70%, Project B = 50%  -> Total 120%  -> Not Allowed
 */
@Service
public class AllocationService {

    private static final Logger logger = LoggerFactory.getLogger(AllocationService.class);
    private static final double MAX_ALLOCATION_PERCENTAGE = 100.0;

    private final AllocationRepository allocationRepository;
    private final EmployeeService employeeService;
    private final ProjectService projectService;
    private final ResourceRequestService resourceRequestService;
    private final AuditService auditService;

    public AllocationService(AllocationRepository allocationRepository, EmployeeService employeeService,
                              ProjectService projectService, ResourceRequestService resourceRequestService,
                              AuditService auditService) {
        this.allocationRepository = allocationRepository;
        this.employeeService = employeeService;
        this.projectService = projectService;
        this.resourceRequestService = resourceRequestService;
        this.auditService = auditService;
    }

    @Transactional
    public AllocationResponse allocateEmployee(AllocationRequest request) {
        Employee employee = employeeService.findEmployeeOrThrow(request.getEmployeeId());
        Project project = projectService.findProjectOrThrow(request.getProjectId());

        ResourceRequest resourceRequest = null;
        if (request.getResourceRequestId() != null) {
            resourceRequest = resourceRequestService.findResourceRequestOrThrow(request.getResourceRequestId());
        }

        validateAllocationCap(employee.getId(), request.getAllocationPercentage(), null);

        Allocation allocation = new Allocation();
        allocation.setEmployee(employee);
        allocation.setProject(project);
        allocation.setResourceRequest(resourceRequest);
        allocation.setAllocationPercentage(request.getAllocationPercentage());
        allocation.setStartDate(request.getStartDate());
        allocation.setEndDate(request.getEndDate());
        allocation.setStatus("ACTIVE");
        allocation.setCreatedBy(SecurityUtil.getCurrentUserEmail());
        allocation.setCreatedDate(LocalDateTime.now().toString());

        Allocation saved = allocationRepository.save(allocation);
        logger.info("Employee id {} allocated {}% to project '{}'", employee.getId(),
                request.getAllocationPercentage(), project.getProjectName());
        auditService.log("Allocation", saved.getId(), "ALLOCATE",
                "Employee allocated " + request.getAllocationPercentage() + "% to project " + project.getProjectName(),
                SecurityUtil.getCurrentUserEmail());

        return toResponse(saved);
    }
    
    @Transactional
    public AllocationResponse reallocateEmployee(Long allocationId, AllocationRequest request) {
        Allocation allocation = findAllocationOrThrow(allocationId);

        // Exclude the current allocation itself from the cap check since it is being replaced
        validateAllocationCap(allocation.getEmployee().getId(), request.getAllocationPercentage(), allocationId);

        if (request.getProjectId() != null) {
            Project project = projectService.findProjectOrThrow(request.getProjectId());
            allocation.setProject(project);
        }
        allocation.setAllocationPercentage(request.getAllocationPercentage());
        if (request.getStartDate() != null) {
            allocation.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            allocation.setEndDate(request.getEndDate());
        }
        allocation.setModifiedBy(SecurityUtil.getCurrentUserEmail());
        allocation.setModifiedDate(LocalDateTime.now().toString());

        Allocation saved = allocationRepository.save(allocation);
        logger.info("Allocation id {} updated to {}%", allocationId, request.getAllocationPercentage());
        auditService.log("Allocation", saved.getId(), "UPDATE", "Allocation reallocated", SecurityUtil.getCurrentUserEmail());

        return toResponse(saved);
    }

    @Transactional
    public AllocationResponse releaseEmployee(Long allocationId) {
        Allocation allocation = findAllocationOrThrow(allocationId);
        allocation.setStatus("RELEASED");
        allocation.setModifiedBy(SecurityUtil.getCurrentUserEmail());
        allocation.setModifiedDate(LocalDateTime.now().toString());

        Allocation saved = allocationRepository.save(allocation);
        logger.info("Allocation id {} released for employee id {}", allocationId, allocation.getEmployee().getId());
        auditService.log("Allocation", saved.getId(), "RELEASE", "Employee released from allocation",
                SecurityUtil.getCurrentUserEmail());

        return toResponse(saved);
    }
    @Transactional(readOnly = true)
    public List<AllocationResponse> getAllAllocations() {
        return allocationRepository.findAll().stream().map(this::toResponse).toList();
    }

    
    @Transactional(readOnly = true)
    public List<AllocationResponse> getAllocationsByEmployee(Long employeeId) {
        return allocationRepository.findByEmployee_Id(employeeId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AllocationResponse> getAllocationsByProject(Long projectId) {
        return allocationRepository.findByProject_Id(projectId).stream().map(this::toResponse).toList();
    }

    /**
     * Enforces the 100% cap across ALL of an employee's currently ACTIVE
     * allocations (not just the single project being allocated).
     * excludeAllocationId lets a reallocation exclude its own previous record.
     */
    private void validateAllocationCap(Long employeeId, Double newPercentage, Long excludeAllocationId) {
        List<Allocation> activeAllocations = allocationRepository.findByEmployee_IdAndStatus(employeeId, "ACTIVE");

        double currentTotal = activeAllocations.stream()
                .filter(a -> excludeAllocationId == null || !a.getId().equals(excludeAllocationId))
                .mapToDouble(Allocation::getAllocationPercentage)
                .sum();

        double prospectiveTotal = currentTotal + newPercentage;

        if (prospectiveTotal > MAX_ALLOCATION_PERCENTAGE) {
            logger.warn("Allocation rejected for employee id {}: current={}%, requested={}%, total would be {}%",
                    employeeId, currentTotal, newPercentage, prospectiveTotal);
            throw new AllocationException(String.format(
                    "Allocation exceeds 100%% cap. Employee already allocated %.2f%% across active projects; "
                            + "adding %.2f%% would total %.2f%%.",
                    currentTotal, newPercentage, prospectiveTotal));
        }
    }

    public Allocation findAllocationOrThrow(Long id) {
        return allocationRepository.findById(id)
                .orElseThrow(() -> new AllocationException("Allocation not found with id: " + id));
    }

    private AllocationResponse toResponse(Allocation allocation) {
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
