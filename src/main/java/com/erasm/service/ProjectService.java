package com.erasm.service;

import com.erasm.dto.ProjectRequest;
import com.erasm.dto.ProjectResponse;
import com.erasm.entity.Project;
import com.erasm.exception.ProjectNotFoundException;
import com.erasm.repository.ProjectRepository;
import com.erasm.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;
    private final AuditService auditService;

    public ProjectService(ProjectRepository projectRepository, AuditService auditService) {
        this.projectRepository = projectRepository;
        this.auditService = auditService;
    }

    public ProjectResponse createProject(ProjectRequest request) {
        Project project = new Project();
        project.setProjectName(request.getProjectName());
        project.setClientName(request.getClientName());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setTechnologyStack(request.getTechnologyStack());
        project.setBudget(request.getBudget());
        project.setStatus("OPEN");
        project.setCreatedBy(SecurityUtil.getCurrentUserEmail());
        project.setCreatedDate(LocalDateTime.now().toString());

        Project saved = projectRepository.save(project);
        logger.info("Project created: {}", saved.getProjectName());
        auditService.log("Project", saved.getId(), "CREATE", "Project created: " + saved.getProjectName(),
                SecurityUtil.getCurrentUserEmail());

        return toResponse(saved);
    }

    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        Project project = findProjectOrThrow(id);

        if (request.getProjectName() != null) {
            project.setProjectName(request.getProjectName());
        }
        if (request.getClientName() != null) {
            project.setClientName(request.getClientName());
        }
        if (request.getStartDate() != null) {
            project.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            project.setEndDate(request.getEndDate());
        }
        if (request.getTechnologyStack() != null) {
            project.setTechnologyStack(request.getTechnologyStack());
        }
        if (request.getBudget() != null) {
            project.setBudget(request.getBudget());
        }
        project.setModifiedBy(SecurityUtil.getCurrentUserEmail());
        project.setModifiedDate(LocalDateTime.now().toString());

        Project saved = projectRepository.save(project);
        logger.info("Project updated: {}", saved.getProjectName());
        auditService.log("Project", saved.getId(), "UPDATE", "Project updated: " + saved.getProjectName(),
                SecurityUtil.getCurrentUserEmail());

        return toResponse(saved);
    }

    public ProjectResponse closeProject(Long id) {
        Project project = findProjectOrThrow(id);
        project.setStatus("CLOSED");
        project.setModifiedBy(SecurityUtil.getCurrentUserEmail());
        project.setModifiedDate(LocalDateTime.now().toString());

        Project saved = projectRepository.save(project);
        logger.info("Project closed: {}", saved.getProjectName());
        auditService.log("Project", saved.getId(), "UPDATE", "Project closed: " + saved.getProjectName(),
                SecurityUtil.getCurrentUserEmail());

        return toResponse(saved);
    }

    public void deleteProject(Long id) {
        Project project = findProjectOrThrow(id);
        projectRepository.delete(project);
        logger.info("Project deleted: {}", project.getProjectName());
        auditService.log("Project", id, "DELETE", "Project deleted: " + project.getProjectName(),
                SecurityUtil.getCurrentUserEmail());
    }

    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll().stream().map(this::toResponse).toList();
    }

    public ProjectResponse getProjectById(Long id) {
        return toResponse(findProjectOrThrow(id));
    }

    Project findProjectOrThrow(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + id));
    }

    private ProjectResponse toResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setProjectName(project.getProjectName());
        response.setClientName(project.getClientName());
        response.setStartDate(project.getStartDate());
        response.setEndDate(project.getEndDate());
        response.setTechnologyStack(project.getTechnologyStack());
        response.setBudget(project.getBudget());
        response.setStatus(project.getStatus());
        return response;
    }
}
