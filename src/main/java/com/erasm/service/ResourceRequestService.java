package com.erasm.service;

import com.erasm.dto.ResourceRequestCreateRequest;
import com.erasm.dto.ResourceRequestResponse;
import com.erasm.entity.Project;
import com.erasm.entity.ResourceRequest;
import com.erasm.entity.Skill;
import com.erasm.exception.InvalidWorkflowStateException;
import com.erasm.exception.ResourceRequestNotFoundException;
import com.erasm.exception.SkillNotFoundException;
import com.erasm.repository.ResourceRequestRepository;
import com.erasm.repository.SkillRepository;
import com.erasm.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Handles Module 5 (Resource Request Management) and Module 6 (Approval
 * Workflow). The workflow strictly follows:
 * DRAFT -> SUBMITTED -> UNDER_REVIEW -> APPROVED -> ALLOCATED -> COMPLETED
 * (REJECTED is a terminal state reachable from UNDER_REVIEW).
 */
@Service
public class ResourceRequestService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceRequestService.class);

    // Defines which transitions are legal from a given current status
    private static final Map<String, List<String>> ALLOWED_TRANSITIONS = Map.of(
            "DRAFT", List.of("SUBMITTED"),
            "SUBMITTED", List.of("UNDER_REVIEW"),
            "UNDER_REVIEW", List.of("APPROVED", "REJECTED"),
            "APPROVED", List.of("ALLOCATED"),
            "ALLOCATED", List.of("COMPLETED"),
            "COMPLETED", List.of(),
            "REJECTED", List.of()
    );

    private final ResourceRequestRepository resourceRequestRepository;
    private final ProjectService projectService;
    private final SkillRepository skillRepository;
    private final AuditService auditService;

    public ResourceRequestService(ResourceRequestRepository resourceRequestRepository, ProjectService projectService,
                                   SkillRepository skillRepository, AuditService auditService) {
        this.resourceRequestRepository = resourceRequestRepository;
        this.projectService = projectService;
        this.skillRepository = skillRepository;
        this.auditService = auditService;
    }

    public ResourceRequestResponse createResourceRequest(ResourceRequestCreateRequest request) {
        Project project = projectService.findProjectOrThrow(request.getProjectId());
        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new SkillNotFoundException("Skill not found with id: " + request.getSkillId()));

        ResourceRequest resourceRequest = new ResourceRequest();
        resourceRequest.setProject(project);
        resourceRequest.setRequiredSkill(skill);
        resourceRequest.setRequiredCount(request.getRequiredCount());
        resourceRequest.setStatus("DRAFT");
        resourceRequest.setCreatedBy(SecurityUtil.getCurrentUserEmail());
        resourceRequest.setCreatedDate(LocalDateTime.now().toString());

        ResourceRequest saved = resourceRequestRepository.save(resourceRequest);
        logger.info("Resource request created for project '{}', skill '{}'", project.getProjectName(),
                skill.getSkillName());
        auditService.log("ResourceRequest", saved.getId(), "CREATE",
                "Resource request created in DRAFT status", SecurityUtil.getCurrentUserEmail());

        return toResponse(saved);
    }

    /**
     * Moves a resource request from its current workflow status to the
     * requested new status, enforcing the state machine defined in
     * ALLOWED_TRANSITIONS.
     */
    public ResourceRequestResponse transitionStatus(Long id, String newStatus, String remarks) {
        ResourceRequest resourceRequest = findResourceRequestOrThrow(id);
        String currentStatus = resourceRequest.getStatus();

        List<String> allowedNext = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, List.of());
        if (!allowedNext.contains(newStatus)) {
            logger.warn("Rejected invalid workflow transition for resource request id {}: {} -> {}", id,
                    currentStatus, newStatus);
            throw new InvalidWorkflowStateException(
                    "Cannot transition resource request from " + currentStatus + " to " + newStatus
                            + ". Allowed next states: " + allowedNext);
        }

        resourceRequest.setStatus(newStatus);
        if (remarks != null) {
            resourceRequest.setRemarks(remarks);
        }
        resourceRequest.setModifiedBy(SecurityUtil.getCurrentUserEmail());
        resourceRequest.setModifiedDate(LocalDateTime.now().toString());

        ResourceRequest saved = resourceRequestRepository.save(resourceRequest);
        logger.info("Resource request id {} transitioned from {} to {}", id, currentStatus, newStatus);
        auditService.log("ResourceRequest", id, "UPDATE",
                "Status changed from " + currentStatus + " to " + newStatus, SecurityUtil.getCurrentUserEmail());

        return toResponse(saved);
    }

    public List<ResourceRequestResponse> getAllResourceRequests() {
        return resourceRequestRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<ResourceRequestResponse> getByProject(Long projectId) {
        return resourceRequestRepository.findByProject_Id(projectId).stream().map(this::toResponse).toList();
    }

    public List<ResourceRequestResponse> getByStatus(String status) {
        return resourceRequestRepository.findByStatus(status).stream().map(this::toResponse).toList();
    }

    public ResourceRequestResponse getById(Long id) {
        return toResponse(findResourceRequestOrThrow(id));
    }

    ResourceRequest findResourceRequestOrThrow(Long id) {
        return resourceRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceRequestNotFoundException("Resource request not found with id: " + id));
    }

    private ResourceRequestResponse toResponse(ResourceRequest rr) {
        ResourceRequestResponse response = new ResourceRequestResponse();
        response.setId(rr.getId());
        response.setProjectId(rr.getProject().getId());
        response.setProjectName(rr.getProject().getProjectName());
        response.setSkillId(rr.getRequiredSkill().getId());
        response.setSkillName(rr.getRequiredSkill().getSkillName());
        response.setRequiredCount(rr.getRequiredCount());
        response.setStatus(rr.getStatus());
        response.setRemarks(rr.getRemarks());
        return response;
    }
}
