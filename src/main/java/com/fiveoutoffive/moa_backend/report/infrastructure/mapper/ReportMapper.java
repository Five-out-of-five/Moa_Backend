package com.fiveoutoffive.moa_backend.report.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.report.domain.model.Report;
import com.fiveoutoffive.moa_backend.report.infrastructure.persistence.ReportJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReportMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    ReportJpaEntity toJpaEntity(Report report);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default Report toDomain(ReportJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return Report.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getReporterAccountId(),
                jpaEntity.getRoomId(),
                jpaEntity.getTargetType(),
                jpaEntity.getTargetId(),
                jpaEntity.getTargetAccountId(),
                jpaEntity.getReason(),
                jpaEntity.getDetail(),
                jpaEntity.getSnapshotJson(),
                jpaEntity.getStatus(),
                jpaEntity.getAction(),
                jpaEntity.getActionReason(),
                jpaEntity.getIsFalseReport(),
                jpaEntity.getHandledByAccountId(),
                jpaEntity.getHandledAt(),
                jpaEntity.getCreatedAt(),
                jpaEntity.getUpdatedAt()
        );
    }
}
