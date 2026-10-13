package com.creditbook.service;

import com.creditbook.dto.AuditLogResponse;
import com.creditbook.entity.AuditLog;
import com.creditbook.entity.User;
import com.creditbook.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void recordLog(User user, String action, String entityType, Long entityId, String details, String ipAddress) {
        AuditLog log = new AuditLog(user, action, entityType, entityId, details, ipAddress);
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getRecentLogs(Long userId) {
        return auditLogRepository.findTop50ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(log -> new AuditLogResponse(
                        log.getId(),
                        log.getAction(),
                        log.getEntityType(),
                        log.getEntityId(),
                        log.getDetails(),
                        log.getIpAddress(),
                        log.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
}
