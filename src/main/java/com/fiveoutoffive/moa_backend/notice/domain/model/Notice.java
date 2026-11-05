package com.fiveoutoffive.moa_backend.notice.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 서비스 공지사항 (notice)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice {

    /** PK */
    private Long id;

    /** 일반 / 점검 / 긴급(상단 배너) (NOT NULL) */
    private NoticeType noticeType;

    /** 제목 (NOT NULL) */
    private String title;

    /** 본문 (Markdown) (NOT NULL) */
    private String content;

    /** 노출 시작 (NOT NULL) */
    private LocalDateTime displayStartsAt;

    /** 노출 종료 (지나면 자동 내림) (NOT NULL) */
    private LocalDateTime displayEndsAt;

    /** 점검 시작 시각 (점검 공지 재노출 기준) */
    private LocalDateTime maintenanceStartsAt;

    /** 작성한 서비스 관리자 (NOT NULL) */
    private Long authorAccountId;

    /** 소프트 딜리트 시각 (NULL = 정상) */
    private LocalDateTime deletedAt;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    /** 수정 시각 */
    private LocalDateTime updatedAt;

    private Notice(
            Long id,
            NoticeType noticeType,
            String title,
            String content,
            LocalDateTime displayStartsAt,
            LocalDateTime displayEndsAt,
            LocalDateTime maintenanceStartsAt,
            Long authorAccountId,
            LocalDateTime deletedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.noticeType = noticeType;
        this.title = title;
        this.content = content;
        this.displayStartsAt = displayStartsAt;
        this.displayEndsAt = displayEndsAt;
        this.maintenanceStartsAt = maintenanceStartsAt;
        this.authorAccountId = authorAccountId;
        this.deletedAt = deletedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static Notice reconstitute(
            Long id,
            NoticeType noticeType,
            String title,
            String content,
            LocalDateTime displayStartsAt,
            LocalDateTime displayEndsAt,
            LocalDateTime maintenanceStartsAt,
            Long authorAccountId,
            LocalDateTime deletedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new Notice(id, noticeType, title, content, displayStartsAt, displayEndsAt, maintenanceStartsAt, authorAccountId, deletedAt, createdAt, updatedAt);
    }
}
