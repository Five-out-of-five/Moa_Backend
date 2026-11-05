package com.fiveoutoffive.moa_backend.integration.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.integration.domain.model.NotionExportStatus;
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

/** 노션 내보내기 이력 (동일 회차는 갱신) (notion_export_log). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "notion_export_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotionExportLogJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "integration_id", nullable = false)
    private Long integrationId;

    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "notion_page_id", length = 100)
    private String notionPageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotionExportStatus status;

    @Column(name = "error_message", length = 255)
    private String errorMessage;

    @Column(name = "exported_at", nullable = false)
    private LocalDateTime exportedAt;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public NotionExportLogJpaEntity(
            Long id,
            Long integrationId,
            Long sessionId,
            String notionPageId,
            NotionExportStatus status,
            String errorMessage,
            LocalDateTime exportedAt
    ) {
        this.id = id;
        this.integrationId = integrationId;
        this.sessionId = sessionId;
        this.notionPageId = notionPageId;
        this.status = status;
        this.errorMessage = errorMessage;
        this.exportedAt = exportedAt;
    }

    // DB 기본값과 같은 값을 채운다. JPA 는 null 을 그대로 INSERT 해서 DB 기본값이 쓰이지 않기 때문이다.
    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (this.exportedAt == null) {
            this.exportedAt = now;
        }
    }
}
