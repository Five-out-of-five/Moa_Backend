package com.fiveoutoffive.moa_backend.notice.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.notice.domain.model.Notice;
import com.fiveoutoffive.moa_backend.notice.infrastructure.persistence.NoticeJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NoticeMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    NoticeJpaEntity toJpaEntity(Notice notice);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default Notice toDomain(NoticeJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return Notice.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getNoticeType(),
                jpaEntity.getTitle(),
                jpaEntity.getContent(),
                jpaEntity.getDisplayStartsAt(),
                jpaEntity.getDisplayEndsAt(),
                jpaEntity.getMaintenanceStartsAt(),
                jpaEntity.getAuthorAccountId(),
                jpaEntity.getDeletedAt(),
                jpaEntity.getCreatedAt(),
                jpaEntity.getUpdatedAt()
        );
    }
}
