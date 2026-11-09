package com.fiveoutoffive.moa_backend.notice.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자가 닫은 공지 (notice_dismissal)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeDismissal {

    /** 공지 */
    private Long noticeId;

    /** 회원 */
    private Long accountId;

    /** 닫은 시각 (NOT NULL) */
    private LocalDateTime dismissedAt;

    private NoticeDismissal(
            Long noticeId,
            Long accountId,
            LocalDateTime dismissedAt
    ) {
        this.noticeId = noticeId;
        this.accountId = accountId;
        this.dismissedAt = dismissedAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static NoticeDismissal reconstitute(
            Long noticeId,
            Long accountId,
            LocalDateTime dismissedAt
    ) {
        return new NoticeDismissal(noticeId, accountId, dismissedAt);
    }
}
