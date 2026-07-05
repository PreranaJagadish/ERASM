package com.erasm.service;

import com.erasm.dto.*;
import org.springframework.transaction.annotation.Transactional;
import com.erasm.entity.*;
import com.erasm.exception.*;
import com.erasm.repository.*;
import com.erasm.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final CertificationRepository certificationRepository;
    private final AuditService auditService;

    public EmployeeService(EmployeeRepository employeeRepository, UserRepository userRepository,
                            SkillRepository skillRepository, EmployeeSkillRepository employeeSkillRepository,
                            CertificationRepository certificationRepository, AuditService auditService) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.employeeSkillRepository = employeeSkillRepository;
        this.certificationRepository = certificationRepository;
        this.auditService = auditService;
    }

    // ---------- Employee profile creation / lookup ----------
    @Transactional
    public EmployeeResponse createEmployeeProfile(Long userId, EmployeeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        if (employeeRepository.findByUser_Id(userId).isPresent()) {
            throw new DuplicateResourceException("An employee profile already exists for this user");
        }

        Employee employee = new Employee();
        employee.setUser(user);
        employee.setRole(user.getRole());
        employee.setDepartment(request.getDepartment());
        employee.setDesignation(request.getDesignation());
        employee.setDateOfJoining(request.getDateOfJoining());
        employee.setCreatedBy(SecurityUtil.getCurrentUserEmail());
        employee.setCreatedDate(LocalDateTime.now().toString());

        Employee saved = employeeRepository.save(employee);
        logger.info("Employee profile created for user: {}", user.getEmail());
        auditService.log("Employee", saved.getId(), "CREATE", "Employee profile created",
                SecurityUtil.getCurrentUserEmail());

        return toResponse(saved);
    }
    
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream().map(this::toResponse).toList();
    }
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        return toResponse(findEmployeeOrThrow(id));
    }

    public Employee getEmployeeEntityByUserEmail(String email) {
        return employeeRepository.findByUser_Email(email)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee profile not found for user: " + email));
    }
    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {

        Employee employee = findEmployeeOrThrow(id);

        employee.setDepartment(request.getDepartment());
        employee.setDesignation(request.getDesignation());
        employee.setDateOfJoining(request.getDateOfJoining());

        employee.setModifiedDate(LocalDateTime.now().toString());

        Employee updated = employeeRepository.save(employee);

        logger.info("Employee updated successfully: {}", id);

        auditService.log(
                "Employee",
                updated.getId(),
                "UPDATE",
                "Employee profile updated",
                SecurityUtil.getCurrentUserEmail());

        return toResponse(updated);
    }
    @Transactional
    public void deleteEmployee(Long id) {

        Employee employee = findEmployeeOrThrow(id);

        employeeRepository.delete(employee);

        logger.info("Employee deleted successfully: {}", id);

        auditService.log(
                "Employee",
                id,
                "DELETE",
                "Employee profile deleted",
                SecurityUtil.getCurrentUserEmail());
    }

    // ---------- Module 3: Employee Skill Profile ----------
    @Transactional
    public EmployeeSkillResponse addSkillToEmployee(Long employeeId, EmployeeSkillRequest request) {
        Employee employee = findEmployeeOrThrow(employeeId);
        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new SkillNotFoundException("Skill not found with id: " + request.getSkillId()));

        if (employeeSkillRepository.findByEmployee_IdAndSkill_Id(employeeId, skill.getId()).isPresent()) {
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

        return toSkillResponse(saved);
    }
    @Transactional
    public EmployeeSkillResponse updateEmployeeSkillLevel(Long employeeId, Long employeeSkillId,
                                                           EmployeeSkillRequest request) {
        EmployeeSkill employeeSkill = employeeSkillRepository.findById(employeeSkillId)
                .orElseThrow(() -> new SkillNotFoundException("Employee skill record not found with id: " + employeeSkillId));

        if (!employeeSkill.getEmployee().getId().equals(employeeId)) {
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
        logger.info("Skill level updated for employee id {} - skill '{}'", employeeId, saved.getSkill().getSkillName());
        auditService.log("EmployeeSkill", saved.getId(), "UPDATE", "Skill level updated",
                SecurityUtil.getCurrentUserEmail());

        return toSkillResponse(saved);
    }
    @Transactional(readOnly = true)
    public List<EmployeeSkillResponse> getEmployeeSkills(Long employeeId) {
        findEmployeeOrThrow(employeeId);
        return employeeSkillRepository.findByEmployee_Id(employeeId).stream().map(this::toSkillResponse).toList();
    }
    @Transactional
    public CertificationResponse addCertification(Long employeeId, CertificationRequest request) {
        Employee employee = findEmployeeOrThrow(employeeId);

        Certification certification = new Certification();
        certification.setEmployee(employee);
        certification.setCertificationName(request.getCertificationName());
        certification.setIssuedBy(request.getIssuedBy());
        certification.setIssuedDate(request.getIssuedDate());

        Certification saved = certificationRepository.save(certification);
        logger.info("Certification '{}' added for employee id {}", saved.getCertificationName(), employeeId);
        auditService.log("Certification", saved.getId(), "CREATE",
                "Certification " + saved.getCertificationName() + " added", SecurityUtil.getCurrentUserEmail());

        return toCertResponse(saved);
    }
    @Transactional(readOnly = true)
    public List<CertificationResponse> getEmployeeCertifications(Long employeeId) {
        findEmployeeOrThrow(employeeId);
        return certificationRepository.findByEmployee_Id(employeeId).stream().map(this::toCertResponse).toList();
    }

    // ---------- Helpers ----------

    Employee findEmployeeOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));
    }

    private EmployeeResponse toResponse(Employee employee) {
        EmployeeResponse response = new EmployeeResponse();
        response.setId(employee.getId());
        response.setUserId(employee.getUser().getId());
        response.setName(employee.getUser().getName());
        response.setEmail(employee.getUser().getEmail());
        response.setRoleName(employee.getRole() != null ? employee.getRole().getRoleName() : null);
        response.setDepartment(employee.getDepartment());
        response.setDesignation(employee.getDesignation());
        response.setDateOfJoining(employee.getDateOfJoining());
        response.setSkills(employee.getEmployeeSkills().stream().map(this::toSkillResponse).toList());
        response.setCertifications(employee.getCertifications().stream().map(this::toCertResponse).toList());
        return response;
    }

    private EmployeeSkillResponse toSkillResponse(EmployeeSkill es) {
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

    private CertificationResponse toCertResponse(Certification c) {
        CertificationResponse response = new CertificationResponse();
        response.setId(c.getId());
        response.setEmployeeId(c.getEmployee().getId());
        response.setCertificationName(c.getCertificationName());
        response.setIssuedBy(c.getIssuedBy());
        response.setIssuedDate(c.getIssuedDate());
        return response;
    }
}
