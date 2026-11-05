package com.fiveoutoffive.moa_backend.briefing.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.briefing.domain.model.BriefingDismissal;
import com.fiveoutoffive.moa_backend.briefing.infrastructure.persistence.BriefingDismissalJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BriefingDismissalMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    BriefingDismissalJpaEntity toJpaEntity(BriefingDismissal briefingDismissal);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default BriefingDismissal toDomain(BriefingDismissalJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return BriefingDismissal.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getRoomId(),
                jpaEntity.getAccountId(),
                jpaEntity.getEventType(),
                jpaEntity.getDismissedAt()
        );
    }
}
