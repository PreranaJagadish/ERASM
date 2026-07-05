package com.erasm.service;

import com.erasm.dto.AllocationRequest;
import com.erasm.entity.*;
import com.erasm.exception.AllocationException;
import com.erasm.repository.AllocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AllocationServiceTest {

    @Mock
    private AllocationRepository allocationRepository;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private ProjectService projectService;
    @Mock
    private ResourceRequestService resourceRequestService;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private AllocationService allocationService;

    private Employee employee;
    private Project project;

    @BeforeEach
    void setUp() {
        Role role = new Role("EMPLOYEE");
        role.setId(1L);

        User user = new User();
        user.setId(1L);
        user.setName("Test Employee");
        user.setEmail("employee@erasm.com");
        user.setRole(role);

        employee = new Employee();
        employee.setId(1L);
        employee.setUser(user);

        project = new Project();
        project.setId(1L);
        project.setProjectName("Healthcare Portal");
    }

    @Test
    void allocation_withinCap_isAllowed() {
        // Employee already has 60% allocated on another project
        Allocation existing = buildAllocation(60.0);
        when(employeeService.findEmployeeOrThrow(1L)).thenReturn(employee);
        when(projectService.findProjectOrThrow(1L)).thenReturn(project);
        when(allocationRepository.findByEmployee_IdAndStatus(1L, "ACTIVE")).thenReturn(List.of(existing));
        when(allocationRepository.save(any(Allocation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AllocationRequest request = new AllocationRequest();
        request.setEmployeeId(1L);
        request.setProjectId(1L);
        request.setAllocationPercentage(40.0); // 60 + 40 = 100, exactly at cap -> allowed

        assertDoesNotThrow(() -> allocationService.allocateEmployee(request));
    }

    @Test
    void allocation_exceedingCapAcrossMultipleProjects_isRejected() {
        // Employee already has 70% allocated on Project A
        Allocation existing = buildAllocation(70.0);
        when(employeeService.findEmployeeOrThrow(1L)).thenReturn(employee);
        when(projectService.findProjectOrThrow(1L)).thenReturn(project);
        when(allocationRepository.findByEmployee_IdAndStatus(1L, "ACTIVE")).thenReturn(List.of(existing));

        AllocationRequest request = new AllocationRequest();
        request.setEmployeeId(1L);
        request.setProjectId(1L);
        request.setAllocationPercentage(50.0); // 70 + 50 = 120 -> should be rejected

        AllocationException exception = assertThrows(AllocationException.class,
                () -> allocationService.allocateEmployee(request));
        assertTrue(exception.getMessage().contains("100"));

        verify(allocationRepository, never()).save(any(Allocation.class));
    }

    @Test
    void allocation_totalAcrossAllActiveProjects_isEnforced_notJustSingleProject() {
        // Employee has 30% on Project A and 40% on Project B = 70% total already
        Allocation allocationA = buildAllocation(30.0);
        Allocation allocationB = buildAllocation(40.0);
        when(employeeService.findEmployeeOrThrow(1L)).thenReturn(employee);
        when(projectService.findProjectOrThrow(1L)).thenReturn(project);
        when(allocationRepository.findByEmployee_IdAndStatus(1L, "ACTIVE"))
                .thenReturn(List.of(allocationA, allocationB));

        AllocationRequest request = new AllocationRequest();
        request.setEmployeeId(1L);
        request.setProjectId(1L);
        request.setAllocationPercentage(40.0); // 30 + 40 + 40 = 110 -> should be rejected

        assertThrows(AllocationException.class, () -> allocationService.allocateEmployee(request));
    }

    private Allocation buildAllocation(double percentage) {
        Allocation allocation = new Allocation();
        allocation.setId((long) (Math.random() * 1000));
        allocation.setEmployee(employee);
        allocation.setProject(project);
        allocation.setAllocationPercentage(percentage);
        allocation.setStatus("ACTIVE");
        return allocation;
    }
}
