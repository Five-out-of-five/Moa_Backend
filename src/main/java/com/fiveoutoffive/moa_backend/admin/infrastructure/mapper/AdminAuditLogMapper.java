package com.fiveoutoffive.moa_backend.admin.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.admin.domain.model.AdminAuditLog;
import com.fiveoutoffive.moa_backend.admin.infrastructure.persistence.AdminAuditLogJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AdminAuditLogMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    AdminAuditLogJpaEntity toJpaEntity(AdminAuditLog adminAuditLog);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default AdminAuditLog toDomain(AdminAuditLogJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return AdminAuditLog.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getAdminAccountId(),
                jpaEntity.getActionType(),
                jpaEntity.getTargetType(),
                jpaEntity.getTargetId(),
                jpaEntity.getDetailJson(),
                jpaEntity.getIpAddress(),
                jpaEntity.getCreatedAt()
        );
    }
}
