package com.fiveoutoffive.moa_backend.admin.domain.repository;

import com.fiveoutoffive.moa_backend.admin.domain.model.AdminAuditLog;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface AdminAuditLogRepository {

    AdminAuditLog save(AdminAuditLog adminAuditLog);

    Optional<AdminAuditLog> findById(Long id);

    Page<AdminAuditLog> search(@Param("adminId") Long adminId, @Param("actionType") String actionType, @Param("targetType") String targetType, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

    int deleteOlderThan(@Param("before") LocalDateTime before);
}
