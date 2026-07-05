package com.erasm.service;

import com.erasm.dto.CertificationRequest;
import com.erasm.entity.Certification;
import com.erasm.entity.Employee;
import com.erasm.entity.Role;
import com.erasm.entity.User;
import com.erasm.exception.CertificationNotFoundException;
import com.erasm.exception.EmployeeNotFoundException;
import com.erasm.repository.CertificationRepository;
import com.erasm.repository.EmployeeRepository;
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
class CertificationServiceImplTest {

    @Mock
    private CertificationRepository certificationRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private CertificationServiceImpl certificationService;

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
    void addCertification_forExistingEmployee_succeeds() {
        CertificationRequest request = new CertificationRequest();
        request.setCertificationName("Oracle Certified Java Programmer");
        request.setIssuedBy("Oracle");
        request.setIssuedDate("2023-06-01");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(certificationRepository.save(any(Certification.class))).thenAnswer(inv -> {
            Certification c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        var response = certificationService.addCertification(1L, request);

        assertEquals("Oracle Certified Java Programmer", response.getCertificationName());
        assertEquals(1L, response.getEmployeeId());
    }

    @Test
    void addCertification_forMissingEmployee_throwsException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        CertificationRequest request = new CertificationRequest();
        request.setCertificationName("AWS Certified Developer");

        assertThrows(EmployeeNotFoundException.class, () -> certificationService.addCertification(99L, request));
    }

    @Test
    void getCertificationById_notFound_throwsException() {
        when(certificationRepository.findById(50L)).thenReturn(Optional.empty());

        assertThrows(CertificationNotFoundException.class, () -> certificationService.getCertificationById(50L));
    }

    @Test
    void updateCertification_belongingToDifferentEmployee_throwsException() {
        Certification certification = new Certification();
        certification.setId(5L);
        certification.setEmployee(employee); // belongs to employee id 1

        when(certificationRepository.findById(5L)).thenReturn(Optional.of(certification));

        CertificationRequest request = new CertificationRequest();
        request.setCertificationName("Updated Name");

        assertThrows(IllegalArgumentException.class,
                () -> certificationService.updateCertification(2L, 5L, request));
    }
}
