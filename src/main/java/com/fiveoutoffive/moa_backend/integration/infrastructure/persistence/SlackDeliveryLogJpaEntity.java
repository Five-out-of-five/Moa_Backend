package com.fiveoutoffive.moa_backend.integration.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.integration.domain.model.SlackDeliveryStatus;
import com.fiveoutoffive.moa_backend.integration.domain.model.SlackEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 슬랙 발송 이력 (slack_delivery_log). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "slack_delivery_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SlackDeliveryLogJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "integration_id", nullable = false)
    private Long integrationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private SlackEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SlackDeliveryStatus status;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "error_message", length = 255)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public SlackDeliveryLogJpaEntity(
            Long id,
            Long integrationId,
            SlackEventType eventType,
            SlackDeliveryStatus status,
            Integer retryCount,
            String errorMessage,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.integrationId = integrationId;
        this.eventType = eventType;
        this.status = status;
        this.retryCount = retryCount;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
    }

    // DB 기본값과 같은 값을 채운다. JPA 는 null 을 그대로 INSERT 해서 DB 기본값이 쓰이지 않기 때문이다.
    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (this.retryCount == null) {
            this.retryCount = 0;
        }
        if (this.createdAt == null) {
            this.createdAt = now;
        }
    }
}
