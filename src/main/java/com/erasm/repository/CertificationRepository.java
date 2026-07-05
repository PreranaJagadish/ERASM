package com.erasm.repository;

import com.erasm.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByEmployee_Id(Long employeeId);
}
