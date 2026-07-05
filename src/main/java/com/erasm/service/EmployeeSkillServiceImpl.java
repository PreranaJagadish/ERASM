package com.erasm.service;

import com.erasm.dto.EmployeeSkillRequest;
import com.erasm.dto.EmployeeSkillResponse;
import com.erasm.entity.Employee;
import com.erasm.entity.EmployeeSkill;
import com.erasm.entity.Skill;
import com.erasm.exception.DuplicateResourceException;
import com.erasm.exception.EmployeeNotFoundException;
import com.erasm.exception.EmployeeSkillNotFoundException;
import com.erasm.exception.SkillNotFoundException;
import com.erasm.repository.EmployeeRepository;
import com.erasm.repository.EmployeeSkillRepository;
import com.erasm.repository.SkillRepository;
import com.erasm.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standalone implementation of {@link EmployeeSkillService}, exposed
 * independently via {@link com.erasm.controller.EmployeeSkillController}
 * in addition to the skill endpoints nested under
 * /api/employees/{employeeId}/skills.
 */
@Service
public class EmployeeSkillServiceImpl implements EmployeeSkillService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeSkillServiceImpl.class);

    private final EmployeeSkillRepository employeeSkillRepository;
    private final EmployeeRepository employeeRepository;
    private final SkillRepository skillRepository;
    private final AuditService auditService;

    public EmployeeSkillServiceImpl(EmployeeSkillRepository employeeSkillRepository,
                                     EmployeeRepository employeeRepository,
                                     SkillRepository skillRepository,
                                     AuditService auditService) {
        this.employeeSkillRepository = employeeSkillRepository;
        this.employeeRepository = employeeRepository;
        this.skillRepository = skillRepository;
        this.auditService = auditService;
    }

    @Override
    public EmployeeSkillResponse addEmployeeSkill(Long employeeId, EmployeeSkillRequest request) {
        Employee employee = findEmployeeOrThrow(employeeId);
        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new SkillNotFoundException("Skill not found with id: " + request.getSkillId()));

        if (employeeSkillRepository.findByEmployee_IdAndSkill_Id(employeeId, skill.getId()).isPresent()) {
            logger.warn("Duplicate skill assignment attempted: employee id {}, skill id {}", employeeId, skill.getId());
            throw new DuplicateResourceException("This skill is already added to the employee profile");
        }

        EmployeeSkill employeeSkill = new EmployeeSkill();
        employeeSkill.setEmployee(employee);
        employeeSkill.setSkill(skill);
        employeeSkill.setProficiencyLevel(request.getProficiencyLevel());
        employeeSkill.setYearsOfExperience(request.getYearsOfExperience());
        employeeSkill.setCreatedDate(LocalDateTime.now().toString());

        EmployeeSkill saved = employeeSkillRepository.save(employeeSkill);
        logger.info("Skill '{}' added to employee id {}", skill.getSkillName(), employeeId);
        auditService.log("EmployeeSkill", saved.getId(), "CREATE",
                "Skill " + skill.getSkillName() + " added to employee " + employeeId,
                SecurityUtil.getCurrentUserEmail());

        return toResponse(saved);
    }

    @Override
    public EmployeeSkillResponse updateEmployeeSkill(Long employeeId, Long employeeSkillId, EmployeeSkillRequest request) {
        EmployeeSkill employeeSkill = findEmployeeSkillOrThrow(employeeSkillId);

        if (!employeeSkill.getEmployee().getId().equals(employeeId)) {
            logger.warn("Attempt to update employee-skill id {} that does not belong to employee id {}",
                    employeeSkillId, employeeId);
            throw new IllegalArgumentException("This skill record does not belong to the given employee");
        }

        if (request.getProficiencyLevel() != null) {
            employeeSkill.setProficiencyLevel(request.getProficiencyLevel());
        }
        if (request.getYearsOfExperience() != null) {
            employeeSkill.setYearsOfExperience(request.getYearsOfExperience());
        }
        employeeSkill.setModifiedDate(LocalDateTime.now().toString());

        EmployeeSkill saved = employeeSkillRepository.save(employeeSkill);
        logger.info("Employee-skill id {} updated for employee id {}", employeeSkillId, employeeId);
        auditService.log("EmployeeSkill", saved.getId(), "UPDATE", "Skill level updated",
                SecurityUtil.getCurrentUserEmail());

        return toResponse(saved);
    }

    @Override
    public void deleteEmployeeSkill(Long employeeId, Long employeeSkillId) {
        EmployeeSkill employeeSkill = findEmployeeSkillOrThrow(employeeSkillId);

        if (!employeeSkill.getEmployee().getId().equals(employeeId)) {
            logger.warn("Attempt to delete employee-skill id {} that does not belong to employee id {}",
                    employeeSkillId, employeeId);
            throw new IllegalArgumentException("This skill record does not belong to the given employee");
        }

        employeeSkillRepository.delete(employeeSkill);
        logger.info("Employee-skill id {} deleted for employee id {}", employeeSkillId, employeeId);
        auditService.log("EmployeeSkill", employeeSkillId, "DELETE", "Employee skill removed",
                SecurityUtil.getCurrentUserEmail());
    }

    @Override
    public List<EmployeeSkillResponse> getSkillsByEmployee(Long employeeId) {
        findEmployeeOrThrow(employeeId);
        return employeeSkillRepository.findByEmployee_Id(employeeId).stream().map(this::toResponse).toList();
    }

    @Override
    public EmployeeSkillResponse getEmployeeSkillById(Long employeeSkillId) {
        return toResponse(findEmployeeSkillOrThrow(employeeSkillId));
    }

    private Employee findEmployeeOrThrow(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + employeeId));
    }

    private EmployeeSkill findEmployeeSkillOrThrow(Long employeeSkillId) {
        return employeeSkillRepository.findById(employeeSkillId)
                .orElseThrow(() -> new EmployeeSkillNotFoundException(
                        "Employee skill record not found with id: " + employeeSkillId));
    }

    private EmployeeSkillResponse toResponse(EmployeeSkill es) {
        EmployeeSkillResponse response = new EmployeeSkillResponse();
        response.setId(es.getId());
        response.setEmployeeId(es.getEmployee().getId());
        response.setEmployeeName(es.getEmployee().getUser().getName());
        response.setSkillId(es.getSkill().getId());
        response.setSkillName(es.getSkill().getSkillName());
        response.setProficiencyLevel(es.getProficiencyLevel());
        response.setYearsOfExperience(es.getYearsOfExperience());
        return response;
    }
}
