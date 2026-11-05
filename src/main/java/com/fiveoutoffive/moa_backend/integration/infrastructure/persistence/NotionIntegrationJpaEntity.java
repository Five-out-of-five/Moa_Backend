package com.fiveoutoffive.moa_backend.integration.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.integration.domain.model.NotionExportMode;
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

/** 노션 연동 (방당 1개, 단방향 내보내기) (notion_integration). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "notion_integration")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotionIntegrationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "access_token_enc", nullable = false)
    private byte[] accessTokenEnc;

    @Column(name = "notion_workspace_id", length = 100)
    private String notionWorkspaceId;

    @Column(name = "notion_workspace_name", length = 100)
    private String notionWorkspaceName;

    @Column(name = "target_database_id", length = 100)
    private String targetDatabaseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "export_mode", nullable = false, length = 20)
    private NotionExportMode exportMode;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @Column(name = "token_expired_at")
    private LocalDateTime tokenExpiredAt;

    @Column(name = "created_by_account_id", nullable = false)
    private Long createdByAccountId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public NotionIntegrationJpaEntity(
            Long id,
            Long roomId,
            byte[] accessTokenEnc,
            String notionWorkspaceId,
            String notionWorkspaceName,
            String targetDatabaseId,
            NotionExportMode exportMode,
            Boolean enabled,
            LocalDateTime tokenExpiredAt,
            Long createdByAccountId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.roomId = roomId;
        this.accessTokenEnc = accessTokenEnc;
        this.notionWorkspaceId = notionWorkspaceId;
        this.notionWorkspaceName = notionWorkspaceName;
        this.targetDatabaseId = targetDatabaseId;
        this.exportMode = exportMode;
        this.enabled = enabled;
        this.tokenExpiredAt = tokenExpiredAt;
        this.createdByAccountId = createdByAccountId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // DB 기본값과 같은 값을 채운다. JPA 는 null 을 그대로 INSERT 해서 DB 기본값이 쓰이지 않기 때문이다.
    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (this.exportMode == null) {
            this.exportMode = NotionExportMode.MANUAL;
        }
        if (this.enabled == null) {
            this.enabled = Boolean.TRUE;
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
