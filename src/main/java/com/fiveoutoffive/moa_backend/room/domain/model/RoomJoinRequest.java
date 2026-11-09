package com.fiveoutoffive.moa_backend.room.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 방 참여 요청 ('승인 필요' 방식) (room_join_request)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomJoinRequest {

    /** PK */
    private Long id;

    /** 방 (NOT NULL) */
    private Long roomId;

    /** 요청자 (NOT NULL) */
    private Long accountId;

    /** 대기 / 승인 / 거절 / 만료 (NOT NULL) */
    private JoinRequestStatus status;

    /** 요청 시각 (NOT NULL) */
    private LocalDateTime requestedAt;

    /** 자동 만료 시각 (요청 + 7일) (NOT NULL) */
    private LocalDateTime expiresAt;

    /** 처리 시각 */
    private LocalDateTime processedAt;

    /** 처리한 방장 */
    private Long processedByAccountId;

    private RoomJoinRequest(
            Long id,
            Long roomId,
            Long accountId,
            JoinRequestStatus status,
            LocalDateTime requestedAt,
            LocalDateTime expiresAt,
            LocalDateTime processedAt,
            Long processedByAccountId
    ) {
        this.id = id;
        this.roomId = roomId;
        this.accountId = accountId;
        this.status = status;
        this.requestedAt = requestedAt;
        this.expiresAt = expiresAt;
        this.processedAt = processedAt;
        this.processedByAccountId = processedByAccountId;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static RoomJoinRequest reconstitute(
            Long id,
            Long roomId,
            Long accountId,
            JoinRequestStatus status,
            LocalDateTime requestedAt,
            LocalDateTime expiresAt,
            LocalDateTime processedAt,
            Long processedByAccountId
    ) {
        return new RoomJoinRequest(id, roomId, accountId, status, requestedAt, expiresAt, processedAt, processedByAccountId);
    }
}
