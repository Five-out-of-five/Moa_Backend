package com.fiveoutoffive.moa_backend.notice.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.notice.domain.model.NoticeDismissal;
import com.fiveoutoffive.moa_backend.notice.infrastructure.persistence.NoticeDismissalJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NoticeDismissalMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    NoticeDismissalJpaEntity toJpaEntity(NoticeDismissal noticeDismissal);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default NoticeDismissal toDomain(NoticeDismissalJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return NoticeDismissal.reconstitute(
                jpaEntity.getNoticeId(),
                jpaEntity.getAccountId(),
                jpaEntity.getDismissedAt()
        );
    }
}
