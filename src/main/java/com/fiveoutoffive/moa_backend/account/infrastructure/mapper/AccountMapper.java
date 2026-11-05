package com.fiveoutoffive.moa_backend.account.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.account.domain.model.Account;
import com.fiveoutoffive.moa_backend.account.infrastructure.persistence.AccountJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    AccountJpaEntity toJpaEntity(Account account);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default Account toDomain(AccountJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return Account.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getProvider(),
                jpaEntity.getProviderUid(),
                jpaEntity.getEmail(),
                jpaEntity.getName(),
                jpaEntity.getRole(),
                jpaEntity.getStatus(),
                jpaEntity.getSuspendedUntil(),
                jpaEntity.getToneMode(),
                jpaEntity.getLastRoomId(),
                jpaEntity.getReportBlockedUntil(),
                jpaEntity.getLastLoginAt(),
                jpaEntity.getTermsAgreedAt(),
                jpaEntity.getPrivacyAgreedAt(),
                jpaEntity.getPurgeAt(),
                jpaEntity.getDeletedAt(),
                jpaEntity.getCreatedAt(),
                jpaEntity.getUpdatedAt()
        );
    }
}
