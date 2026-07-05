package com.erasm.repository;

import com.erasm.entity.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AllocationRepository extends JpaRepository<Allocation, Long> {
    List<Allocation> findByEmployee_Id(Long employeeId);
    List<Allocation> findByEmployee_IdAndStatus(Long employeeId, String status);
    List<Allocation> findByProject_Id(Long projectId);
}
