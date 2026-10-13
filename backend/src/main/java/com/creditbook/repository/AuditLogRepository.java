package com.creditbook.repository;

import com.creditbook.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findTop50ByUserIdOrderByCreatedAtDesc(Long userId);

    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<AuditLog> findByUserIdAndEntityTypeOrderByCreatedAtDesc(Long userId, String entityType);
}
