package com.erasm.service;

import com.erasm.entity.Project;
import com.erasm.entity.ResourceRequest;
import com.erasm.entity.Skill;
import com.erasm.exception.InvalidWorkflowStateException;
import com.erasm.repository.ResourceRequestRepository;
import com.erasm.repository.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceRequestServiceTest {

    @Mock
    private ResourceRequestRepository resourceRequestRepository;
    @Mock
    private ProjectService projectService;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private ResourceRequestService resourceRequestService;

    private ResourceRequest resourceRequest;

    @BeforeEach
    void setUp() {
        Project project = new Project();
        project.setId(1L);
        project.setProjectName("Healthcare Portal");

        Skill skill = new Skill();
        skill.setId(1L);
        skill.setSkillName("Java");

        resourceRequest = new ResourceRequest();
        resourceRequest.setId(1L);
        resourceRequest.setProject(project);
        resourceRequest.setRequiredSkill(skill);
        resourceRequest.setRequiredCount(3);
        resourceRequest.setStatus("DRAFT");
    }

    @Test
    void validTransition_draftToSubmitted_succeeds() {
        when(resourceRequestRepository.findById(1L)).thenReturn(java.util.Optional.of(resourceRequest));
        when(resourceRequestRepository.save(any(ResourceRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = resourceRequestService.transitionStatus(1L, "SUBMITTED", "Submitting for review");

        assertEquals("SUBMITTED", response.getStatus());
    }

    @Test
    void invalidTransition_draftDirectlyToApproved_throwsException() {
        when(resourceRequestRepository.findById(1L)).thenReturn(java.util.Optional.of(resourceRequest));

        assertThrows(InvalidWorkflowStateException.class,
                () -> resourceRequestService.transitionStatus(1L, "APPROVED", null));
    }

    @Test
    void invalidTransition_fromCompletedState_isRejected() {
        resourceRequest.setStatus("COMPLETED");
        when(resourceRequestRepository.findById(1L)).thenReturn(java.util.Optional.of(resourceRequest));

        assertThrows(InvalidWorkflowStateException.class,
                () -> resourceRequestService.transitionStatus(1L, "SUBMITTED", null));
    }
}
