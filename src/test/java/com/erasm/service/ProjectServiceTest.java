package com.erasm.service;

import com.erasm.dto.ProjectRequest;
import com.erasm.entity.Project;
import com.erasm.exception.ProjectNotFoundException;
import com.erasm.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void createProject_setsStatusToOpenByDefault() {
        ProjectRequest request = new ProjectRequest();
        request.setProjectName("Healthcare Portal");
        request.setClientName("Acme Health");
        request.setBudget(500000.0);

        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> {
            Project p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        var response = projectService.createProject(request);

        assertEquals("OPEN", response.getStatus());
        assertEquals("Healthcare Portal", response.getProjectName());
    }

    @Test
    void closeProject_setsStatusToClosed() {
        Project project = new Project();
        project.setId(1L);
        project.setProjectName("Healthcare Portal");
        project.setStatus("OPEN");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = projectService.closeProject(1L);

        assertEquals("CLOSED", response.getStatus());
    }

    @Test
    void getProjectById_notFound_throwsException() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class, () -> projectService.getProjectById(99L));
    }
}
