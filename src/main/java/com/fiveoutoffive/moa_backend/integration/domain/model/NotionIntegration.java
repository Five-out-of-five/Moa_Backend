package com.fiveoutoffive.moa_backend.integration.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 노션 연동 (방당 1개, 단방향 내보내기) (notion_integration)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotionIntegration {

    /** PK */
    private Long id;

    /** 방 (NOT NULL) */
    private Long roomId;

    /** 암호화된 노션 액세스 토큰 (NOT NULL) */
    private byte[] accessTokenEnc;

    /** 노션 워크스페이스 ID */
    private String notionWorkspaceId;

    /** 노션 워크스페이스 이름 */
    private String notionWorkspaceName;

    /** 내보낼 노션 데이터베이스 ID */
    private String targetDatabaseId;

    /** 수동 / 회차 종료 시 자동 (NOT NULL) */
    private NotionExportMode exportMode;

    /** 활성 여부 (NOT NULL) */
    private Boolean enabled;

    /** 권한 만료 감지 시각 (재연결 안내) */
    private LocalDateTime tokenExpiredAt;

    /** 연결한 방장 (NOT NULL) */
    private Long createdByAccountId;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    /** 수정 시각 */
    private LocalDateTime updatedAt;

    private NotionIntegration(
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

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static NotionIntegration reconstitute(
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
        return new NotionIntegration(id, roomId, accessTokenEnc, notionWorkspaceId, notionWorkspaceName, targetDatabaseId, exportMode, enabled, tokenExpiredAt, createdByAccountId, createdAt, updatedAt);
    }
}
