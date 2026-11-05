package com.fiveoutoffive.moa_backend.ai.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.ai.domain.model.AiFeature;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** AI 기능별 일일 사용량 (방별 호출 상한 관리) (ai_usage_quota). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "ai_usage_quota")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiUsageQuotaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Enumerated(EnumType.STRING)
    @Column(name = "feature", nullable = false, length = 20)
    private AiFeature feature;

    @Column(name = "quota_date", nullable = false)
    private LocalDate quotaDate;

    @Column(name = "used_count", nullable = false)
    private Integer usedCount;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public AiUsageQuotaJpaEntity(
            Long id,
            Long roomId,
            AiFeature feature,
            LocalDate quotaDate,
            Integer usedCount
    ) {
        this.id = id;
        this.roomId = roomId;
        this.feature = feature;
        this.quotaDate = quotaDate;
        this.usedCount = usedCount;
    }

    // DB 기본값과 같은 값을 채운다. JPA 는 null 을 그대로 INSERT 해서 DB 기본값이 쓰이지 않기 때문이다.
    @PrePersist
    void prePersist() {
        if (this.usedCount == null) {
            this.usedCount = 0;
        }
    }
}
