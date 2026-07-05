package com.erasm.repository;

import com.erasm.entity.Employee;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @EntityGraph(attributePaths = {
            "user",
            "role",
            "employeeSkills",
            "employeeSkills.skill",
            "certifications"
    })
    @Override
    Optional<Employee> findById(Long id);

    @EntityGraph(attributePaths = {
            "user",
            "role",
            "employeeSkills",
            "employeeSkills.skill",
            "certifications"
    })
    @Override
    List<Employee> findAll();

    @EntityGraph(attributePaths = {
            "user",
            "role",
            "employeeSkills",
            "employeeSkills.skill",
            "certifications"
    })
    Optional<Employee> findByUser_Id(Long userId);

    @EntityGraph(attributePaths = {
            "user",
            "role",
            "employeeSkills",
            "employeeSkills.skill",
            "certifications"
    })
    Optional<Employee> findByUser_Email(String email);
}