package com.fiveoutoffive.moa_backend.admin.infrastructure.persistence;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataAdminAuditLogRepository extends JpaRepository<AdminAuditLogJpaEntity, Long> {

    @Query("select l from AdminAuditLogJpaEntity l where (:adminId is null or l.adminAccountId = :adminId) and (:actionType is null or l.actionType = :actionType) and (:targetType is null or l.targetType = :targetType) and (cast(:from as timestamp) is null or l.createdAt >= :from) and (cast(:to as timestamp) is null or l.createdAt < :to)")
    Page<AdminAuditLogJpaEntity> search(@Param("adminId") Long adminId, @Param("actionType") String actionType, @Param("targetType") String targetType, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

    @Modifying
    @Query("delete from AdminAuditLogJpaEntity l where l.createdAt < :before")
    int deleteOlderThan(@Param("before") LocalDateTime before);
}
