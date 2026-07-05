package com.erasm.service;

import com.erasm.dto.UtilizationResponse;
import com.erasm.entity.Allocation;
import com.erasm.entity.Employee;
import com.erasm.repository.AllocationRepository;
import com.erasm.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Module 8: Utilization Dashboard.
 *
 * Billable % = (Billable Hours / Total Hours) x 100
 * Bench %    = (Bench Hours / Total Hours) x 100
 *
 * Since ERASM tracks employee workload as an allocation PERCENTAGE per
 * project rather than raw hours, the total ACTIVE allocation percentage is
 * used as a direct proxy for billable hours out of a 100% total capacity,
 * and the remainder is bench time. This keeps the formula's spirit
 * (billable + bench = total capacity) while matching the data actually
 * captured by the Allocation module.
 */
@Service
public class DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

    private final EmployeeRepository employeeRepository;
    private final AllocationRepository allocationRepository;

    public DashboardService(EmployeeRepository employeeRepository, AllocationRepository allocationRepository) {
        this.employeeRepository = employeeRepository;
        this.allocationRepository = allocationRepository;
    }

    public List<UtilizationResponse> getUtilizationForAllEmployees() {
        return employeeRepository.findAll().stream().map(this::buildUtilization).toList();
    }

    public UtilizationResponse getUtilizationForEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new com.erasm.exception.EmployeeNotFoundException(
                        "Employee not found with id: " + employeeId));
        return buildUtilization(employee);
    }

    private UtilizationResponse buildUtilization(Employee employee) {
        List<Allocation> activeAllocations = allocationRepository.findByEmployee_IdAndStatus(employee.getId(), "ACTIVE");

        double totalAllocated = activeAllocations.stream()
                .mapToDouble(Allocation::getAllocationPercentage)
                .sum();

        double billablePercentage = Math.min(totalAllocated, 100.0);
        double benchPercentage = Math.max(100.0 - billablePercentage, 0.0);

        logger.debug("Utilization computed for employee id {}: billable={}%, bench={}%", employee.getId(),
                billablePercentage, benchPercentage);

        return new UtilizationResponse(
                employee.getId(),
                employee.getUser().getName(),
                totalAllocated,
                billablePercentage,
                benchPercentage);
    }
}
