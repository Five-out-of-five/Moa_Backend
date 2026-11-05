package com.fiveoutoffive.moa_backend.briefing.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 캐치업 브리핑 '팀 소식' 개별 소거 (방·회원·이벤트 종류별) (briefing_dismissal)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BriefingDismissal {

    /** PK */
    private Long id;

    /** 방 (NOT NULL) */
    private Long roomId;

    /** 회원 (NOT NULL) */
    private Long accountId;

    /** 소거한 이벤트 종류 묶음 (NOT NULL) */
    private BoardEventType eventType;

    /** 소거 시각 (이후 생긴 같은 종류 이벤트는 다시 표시) (NOT NULL) */
    private LocalDateTime dismissedAt;

    private BriefingDismissal(
            Long id,
            Long roomId,
            Long accountId,
            BoardEventType eventType,
            LocalDateTime dismissedAt
    ) {
        this.id = id;
        this.roomId = roomId;
        this.accountId = accountId;
        this.eventType = eventType;
        this.dismissedAt = dismissedAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static BriefingDismissal reconstitute(
            Long id,
            Long roomId,
            Long accountId,
            BoardEventType eventType,
            LocalDateTime dismissedAt
    ) {
        return new BriefingDismissal(id, roomId, accountId, eventType, dismissedAt);
    }
}
