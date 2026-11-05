package com.fiveoutoffive.moa_backend.integration.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** 슬랙 Incoming Webhook 연동 (방당 1개) (slack_integration). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "slack_integration")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SlackIntegrationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "webhook_url_enc", nullable = false)
    private byte[] webhookUrlEnc;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "event_types", nullable = false)
    private String[] eventTypes;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @Column(name = "consecutive_fail_count", nullable = false)
    private Integer consecutiveFailCount;

    @Column(name = "disabled_at")
    private LocalDateTime disabledAt;

    @Column(name = "created_by_account_id", nullable = false)
    private Long createdByAccountId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public SlackIntegrationJpaEntity(
            Long id,
            Long roomId,
            byte[] webhookUrlEnc,
            String[] eventTypes,
            Boolean enabled,
            Integer consecutiveFailCount,
            LocalDateTime disabledAt,
            Long createdByAccountId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.roomId = roomId;
        this.webhookUrlEnc = webhookUrlEnc;
        this.eventTypes = eventTypes;
        this.enabled = enabled;
        this.consecutiveFailCount = consecutiveFailCount;
        this.disabledAt = disabledAt;
        this.createdByAccountId = createdByAccountId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // DB 기본값과 같은 값을 채운다. JPA 는 null 을 그대로 INSERT 해서 DB 기본값이 쓰이지 않기 때문이다.
    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (this.enabled == null) {
            this.enabled = Boolean.TRUE;
        }
        if (this.consecutiveFailCount == null) {
            this.consecutiveFailCount = 0;
        }
        if (this.createdAt == null) {
            this.createdAt = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
