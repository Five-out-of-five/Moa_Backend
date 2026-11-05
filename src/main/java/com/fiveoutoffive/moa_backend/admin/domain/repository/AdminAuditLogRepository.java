package com.fiveoutoffive.moa_backend.admin.domain.repository;

import com.fiveoutoffive.moa_backend.admin.domain.model.AdminAuditLog;
import java.util.Optional;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface AdminAuditLogRepository {

    AdminAuditLog save(AdminAuditLog adminAuditLog);

    Optional<AdminAuditLog> findById(Long id);
}
