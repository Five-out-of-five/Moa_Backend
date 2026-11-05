package com.fiveoutoffive.moa_backend.admin.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.admin.domain.model.AdminAuditLog;
import com.fiveoutoffive.moa_backend.admin.domain.repository.AdminAuditLogRepository;
import com.fiveoutoffive.moa_backend.admin.infrastructure.mapper.AdminAuditLogMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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
}
