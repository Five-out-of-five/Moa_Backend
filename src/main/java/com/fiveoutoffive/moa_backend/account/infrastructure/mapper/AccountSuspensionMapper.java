package com.fiveoutoffive.moa_backend.account.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.account.domain.model.AccountSuspension;
import com.fiveoutoffive.moa_backend.account.infrastructure.persistence.AccountSuspensionJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountSuspensionMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    AccountSuspensionJpaEntity toJpaEntity(AccountSuspension accountSuspension);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default AccountSuspension toDomain(AccountSuspensionJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return AccountSuspension.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getAccountId(),
                jpaEntity.getAdminAccountId(),
                jpaEntity.getReportId(),
                jpaEntity.getPeriod(),
                jpaEntity.getReason(),
                jpaEntity.getStartedAt(),
                jpaEntity.getEndsAt(),
                jpaEntity.getReleasedAt(),
                jpaEntity.getReleasedByAccountId(),
                jpaEntity.getCreatedAt()
        );
    }
}
