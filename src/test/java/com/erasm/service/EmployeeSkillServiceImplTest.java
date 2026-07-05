package com.erasm.service;

import com.erasm.dto.EmployeeSkillRequest;
import com.erasm.entity.Employee;
import com.erasm.entity.EmployeeSkill;
import com.erasm.entity.Role;
import com.erasm.entity.Skill;
import com.erasm.entity.User;
import com.erasm.exception.DuplicateResourceException;
import com.erasm.exception.EmployeeSkillNotFoundException;
import com.erasm.repository.EmployeeRepository;
import com.erasm.repository.EmployeeSkillRepository;
import com.erasm.repository.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
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
class EmployeeSkillServiceImplTest {

    @Mock
    private EmployeeSkillRepository employeeSkillRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private EmployeeSkillServiceImpl employeeSkillService;

    private Employee employee;
    private Skill skill;

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

        skill = new Skill();
        skill.setId(1L);
        skill.setSkillName("Java");
    }

    @Test
    void addEmployeeSkill_new_succeeds() {
        EmployeeSkillRequest request = new EmployeeSkillRequest();
        request.setSkillId(1L);
        request.setProficiencyLevel("Advanced");
        request.setYearsOfExperience(4.0);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));
        when(employeeSkillRepository.findByEmployee_IdAndSkill_Id(1L, 1L)).thenReturn(Optional.empty());
        when(employeeSkillRepository.save(any(EmployeeSkill.class))).thenAnswer(inv -> {
            EmployeeSkill es = inv.getArgument(0);
            es.setId(1L);
            return es;
        });

        var response = employeeSkillService.addEmployeeSkill(1L, request);

        assertEquals("Java", response.getSkillName());
        assertEquals("Advanced", response.getProficiencyLevel());
    }

    @Test
    void addEmployeeSkill_duplicate_throwsException() {
        EmployeeSkillRequest request = new EmployeeSkillRequest();
        request.setSkillId(1L);
        request.setProficiencyLevel("Advanced");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));
        when(employeeSkillRepository.findByEmployee_IdAndSkill_Id(1L, 1L))
                .thenReturn(Optional.of(new EmployeeSkill()));

        assertThrows(DuplicateResourceException.class, () -> employeeSkillService.addEmployeeSkill(1L, request));
    }

    @Test
    void getEmployeeSkillById_notFound_throwsException() {
        when(employeeSkillRepository.findById(77L)).thenReturn(Optional.empty());

        assertThrows(EmployeeSkillNotFoundException.class, () -> employeeSkillService.getEmployeeSkillById(77L));
    }
}
