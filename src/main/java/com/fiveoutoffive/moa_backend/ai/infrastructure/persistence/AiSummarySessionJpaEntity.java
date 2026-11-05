package com.fiveoutoffive.moa_backend.ai.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 요약 대상 회차 (여러 회차 지정) (ai_summary_session). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "ai_summary_session")
@IdClass(AiSummarySessionJpaId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiSummarySessionJpaEntity {

    @Id
    @Column(name = "summary_id", nullable = false)
    private Long summaryId;

    @Id
    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public AiSummarySessionJpaEntity(
            Long summaryId,
            Long sessionId
    ) {
        this.summaryId = summaryId;
        this.sessionId = sessionId;
    }
}
