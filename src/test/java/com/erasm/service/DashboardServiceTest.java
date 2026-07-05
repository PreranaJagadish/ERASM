package com.erasm.service;

import com.erasm.entity.Allocation;
import com.erasm.entity.Employee;
import com.erasm.entity.Role;
import com.erasm.entity.User;
import com.erasm.exception.EmployeeNotFoundException;
import com.erasm.repository.AllocationRepository;
import com.erasm.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private AllocationRepository allocationRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        Role role = new Role("EMPLOYEE");
        role.setId(1L);
        User user = new User();
        user.setId(1L);
        user.setName("Jane Employee");
        user.setEmail("jane@erasm.com");
        user.setRole(role);

        employee = new Employee();
        employee.setId(1L);
        employee.setUser(user);
    }

    @Test
    void utilization_withSixtyPercentAllocated_yieldsFortyPercentBench() {
        Allocation allocation = new Allocation();
        allocation.setAllocationPercentage(60.0);
        allocation.setStatus("ACTIVE");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(allocationRepository.findByEmployee_IdAndStatus(1L, "ACTIVE")).thenReturn(List.of(allocation));

        var response = dashboardService.getUtilizationForEmployee(1L);

        assertEquals(60.0, response.getBillablePercentage());
        assertEquals(40.0, response.getBenchPercentage());
    }

    @Test
    void utilization_withNoActiveAllocations_isFullyOnBench() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(allocationRepository.findByEmployee_IdAndStatus(1L, "ACTIVE")).thenReturn(List.of());

        var response = dashboardService.getUtilizationForEmployee(1L);

        assertEquals(0.0, response.getBillablePercentage());
        assertEquals(100.0, response.getBenchPercentage());
    }

    @Test
    void utilization_forMissingEmployee_throwsException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> dashboardService.getUtilizationForEmployee(99L));
    }
}
