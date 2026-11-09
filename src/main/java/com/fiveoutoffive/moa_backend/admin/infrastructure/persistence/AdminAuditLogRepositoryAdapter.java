package com.fiveoutoffive.moa_backend.admin.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.admin.domain.model.AdminAuditLog;
import com.fiveoutoffive.moa_backend.admin.domain.repository.AdminAuditLogRepository;
import com.fiveoutoffive.moa_backend.admin.infrastructure.mapper.AdminAuditLogMapper;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdminAuditLogRepositoryAdapter implements AdminAuditLogRepository {

    private final SpringDataAdminAuditLogRepository springDataRepository;
    private final AdminAuditLogMapper adminAuditLogMapper;

    @Override
    public AdminAuditLog save(AdminAuditLog adminAuditLog) {
        AdminAuditLogJpaEntity saved = springDataRepository.save(adminAuditLogMapper.toJpaEntity(adminAuditLog));
        return adminAuditLogMapper.toDomain(saved);
    }

    @Override
    public Optional<AdminAuditLog> findById(Long id) {
        return springDataRepository.findById(id)
                .map(adminAuditLogMapper::toDomain);
    }

    @Override
    public Page<AdminAuditLog> search(@Param("adminId") Long adminId, @Param("actionType") String actionType, @Param("targetType") String targetType, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable) {
        return springDataRepository.search(adminId, actionType, targetType, from, to, pageable).map(adminAuditLogMapper::toDomain);
    }

    @Override
    public int deleteOlderThan(@Param("before") LocalDateTime before) {
        return springDataRepository.deleteOlderThan(before);
    }
}
