package com.fiveoutoffive.moa_backend.admin.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAdminAuditLogRepository extends JpaRepository<AdminAuditLogJpaEntity, Long> {
}
