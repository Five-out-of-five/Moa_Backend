package com.fiveoutoffive.moa_backend.account.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.account.domain.model.AccountDailyActivity;
import com.fiveoutoffive.moa_backend.account.infrastructure.persistence.AccountDailyActivityJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountDailyActivityMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    AccountDailyActivityJpaEntity toJpaEntity(AccountDailyActivity accountDailyActivity);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default AccountDailyActivity toDomain(AccountDailyActivityJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return AccountDailyActivity.reconstitute(
                jpaEntity.getAccountId(),
                jpaEntity.getActivityDate()
        );
    }
}
