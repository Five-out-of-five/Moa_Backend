package com.fiveoutoffive.moa_backend.report.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.report.domain.model.ReportAction;
import com.fiveoutoffive.moa_backend.report.domain.model.ReportReason;
import com.fiveoutoffive.moa_backend.report.domain.model.ReportStatus;
import com.fiveoutoffive.moa_backend.report.domain.model.ReportTargetType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

/** 신고 (report). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "reporter_account_id", nullable = false)
    private Long reporterAccountId;

    @Column(name = "room_id")
    private Long roomId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private ReportTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "target_account_id")
    private Long targetAccountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 30)
    private ReportReason reason;

    @Column(name = "detail", length = 500)
    private String detail;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot_json", nullable = false)
    private String snapshotJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReportStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", length = 30)
    private ReportAction action;

    @Column(name = "action_reason", length = 500)
    private String actionReason;

    @Column(name = "is_false_report", nullable = false)
    private Boolean isFalseReport;

    @Column(name = "handled_by_account_id")
    private Long handledByAccountId;

    @Column(name = "handled_at")
    private LocalDateTime handledAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public ReportJpaEntity(
            Long id,
            Long reporterAccountId,
            Long roomId,
            ReportTargetType targetType,
            Long targetId,
            Long targetAccountId,
            ReportReason reason,
            String detail,
            String snapshotJson,
            ReportStatus status,
            ReportAction action,
            String actionReason,
            Boolean isFalseReport,
            Long handledByAccountId,
            LocalDateTime handledAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.reporterAccountId = reporterAccountId;
        this.roomId = roomId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.targetAccountId = targetAccountId;
        this.reason = reason;
        this.detail = detail;
        this.snapshotJson = snapshotJson;
        this.status = status;
        this.action = action;
        this.actionReason = actionReason;
        this.isFalseReport = isFalseReport;
        this.handledByAccountId = handledByAccountId;
        this.handledAt = handledAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // DB 기본값과 같은 값을 채운다. JPA 는 null 을 그대로 INSERT 해서 DB 기본값이 쓰이지 않기 때문이다.
    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (this.status == null) {
            this.status = ReportStatus.RECEIVED;
        }
        if (this.isFalseReport == null) {
            this.isFalseReport = Boolean.FALSE;
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
