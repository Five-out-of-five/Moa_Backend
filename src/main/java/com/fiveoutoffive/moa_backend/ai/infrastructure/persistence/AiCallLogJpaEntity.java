package com.fiveoutoffive.moa_backend.ai.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.ai.domain.model.AiCallStatus;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** AI 호출 로그 (비용·통계, 지난 DB ai_agent_log 계승) (ai_call_log). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "ai_call_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiCallLogJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "feature", nullable = false, length = 20)
    private AiFeature feature;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "ref_type", length = 30)
    private String refType;

    @Column(name = "ref_id")
    private Long refId;

    @Column(name = "model", length = 50)
    private String model;

    @Column(name = "prompt_tokens")
    private Integer promptTokens;

    @Column(name = "output_tokens")
    private Integer outputTokens;

    @Column(name = "cost_amount", precision = 12, scale = 4)
    private BigDecimal costAmount;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AiCallStatus status;

    @Column(name = "error_message", length = 255)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public AiCallLogJpaEntity(
            Long id,
            AiFeature feature,
            Long roomId,
            Long accountId,
            String refType,
            Long refId,
            String model,
            Integer promptTokens,
            Integer outputTokens,
            BigDecimal costAmount,
            Integer latencyMs,
            AiCallStatus status,
            String errorMessage,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.feature = feature;
        this.roomId = roomId;
        this.accountId = accountId;
        this.refType = refType;
        this.refId = refId;
        this.model = model;
        this.promptTokens = promptTokens;
        this.outputTokens = outputTokens;
        this.costAmount = costAmount;
        this.latencyMs = latencyMs;
        this.status = status;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
    }

    // DB 기본값과 같은 값을 채운다. JPA 는 null 을 그대로 INSERT 해서 DB 기본값이 쓰이지 않기 때문이다.
    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (this.createdAt == null) {
            this.createdAt = now;
        }
    }
}
