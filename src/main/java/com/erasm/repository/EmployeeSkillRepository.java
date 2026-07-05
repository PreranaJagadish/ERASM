package com.erasm.repository;

import com.erasm.entity.EmployeeSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, Long> {
    List<EmployeeSkill> findByEmployee_Id(Long employeeId);
    List<EmployeeSkill> findBySkill_Id(Long skillId);
    Optional<EmployeeSkill> findByEmployee_IdAndSkill_Id(Long employeeId, Long skillId);
}
