package com.fiveoutoffive.moa_backend.integration.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 노션 내보내기 이력 (동일 회차는 갱신) (notion_export_log)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotionExportLog {

    /** PK */
    private Long id;

    /** 연동 (NOT NULL) */
    private Long integrationId;

    /** 내보낸 회차 */
    private Long sessionId;

    /** 생성·갱신한 노션 페이지 ID */
    private String notionPageId;

    /** 결과 (NOT NULL) */
    private NotionExportStatus status;

    /** 실패 사유 */
    private String errorMessage;

    /** 내보낸 시각 (NOT NULL) */
    private LocalDateTime exportedAt;

    private NotionExportLog(
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

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static NotionExportLog reconstitute(
            Long id,
            Long integrationId,
            Long sessionId,
            String notionPageId,
            NotionExportStatus status,
            String errorMessage,
            LocalDateTime exportedAt
    ) {
        return new NotionExportLog(id, integrationId, sessionId, notionPageId, status, errorMessage, exportedAt);
    }
}
