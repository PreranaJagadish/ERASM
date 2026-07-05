package com.erasm.service;

import com.erasm.dto.EmployeeSkillRequest;
import com.erasm.dto.EmployeeSkillResponse;

import java.util.List;

/**
 * Dedicated service contract for managing an Employee's skill profile
 * entries (part of Module 3: Employee Skill Profile / the Employee<->Skill
 * Many-to-Many relationship).
 */
public interface EmployeeSkillService {

    EmployeeSkillResponse addEmployeeSkill(Long employeeId, EmployeeSkillRequest request);

    EmployeeSkillResponse updateEmployeeSkill(Long employeeId, Long employeeSkillId, EmployeeSkillRequest request);

    void deleteEmployeeSkill(Long employeeId, Long employeeSkillId);

    List<EmployeeSkillResponse> getSkillsByEmployee(Long employeeId);

    EmployeeSkillResponse getEmployeeSkillById(Long employeeSkillId);
}
