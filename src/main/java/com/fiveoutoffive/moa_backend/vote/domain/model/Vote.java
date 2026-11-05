package com.fiveoutoffive.moa_backend.vote.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 투표 (vote)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vote {

    /** PK */
    private Long id;

    /** 방 (NOT NULL) */
    private Long roomId;

    /** 연결된 안건 카드 */
    private Long sourceCardId;

    /** 재투표의 원 투표 */
    private Long parentVoteId;

    /** 제목 (NOT NULL) */
    private String title;

    /** 단일 / 복수 / 찬반 (생성 후 변경 불가) (NOT NULL) */
    private VoteType voteType;

    /** 최대 선택 개수 (NOT NULL) */
    private Integer maxSelectCount;

    /** 익명 여부 (생성 후 변경 불가) (NOT NULL) */
    private Boolean isAnonymous;

    /** 진행 / 마감 (NOT NULL) */
    private VoteStatus status;

    /** 마감 시각 (생성 시점 + 1시간 이후) (NOT NULL) */
    private LocalDateTime deadlineAt;

    /** 마감 사유 */
    private VoteCloseReason closeReason;

    /** 마감 시각 */
    private LocalDateTime closedAt;

    /** 수동 마감한 멤버 */
    private Long closedByAccountId;

    /** 생성자 (NOT NULL) */
    private Long creatorAccountId;

    /** 미참여자 참여 요청 알림 발송 시각 (1회만) */
    private LocalDateTime reminderSentAt;

    /** 낙관적 락 (수정 도중 다른 멤버가 참여하면 수정 반영 안 함) (NOT NULL) */
    private Long version;

    /** 소프트 딜리트 시각 (NULL = 정상) */
    private LocalDateTime deletedAt;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    /** 수정 시각 */
    private LocalDateTime updatedAt;

    private Vote(
            Long id,
            Long roomId,
            Long sourceCardId,
            Long parentVoteId,
            String title,
            VoteType voteType,
            Integer maxSelectCount,
            Boolean isAnonymous,
            VoteStatus status,
            LocalDateTime deadlineAt,
            VoteCloseReason closeReason,
            LocalDateTime closedAt,
            Long closedByAccountId,
            Long creatorAccountId,
            LocalDateTime reminderSentAt,
            Long version,
            LocalDateTime deletedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.roomId = roomId;
        this.sourceCardId = sourceCardId;
        this.parentVoteId = parentVoteId;
        this.title = title;
        this.voteType = voteType;
        this.maxSelectCount = maxSelectCount;
        this.isAnonymous = isAnonymous;
        this.status = status;
        this.deadlineAt = deadlineAt;
        this.closeReason = closeReason;
        this.closedAt = closedAt;
        this.closedByAccountId = closedByAccountId;
        this.creatorAccountId = creatorAccountId;
        this.reminderSentAt = reminderSentAt;
        this.version = version;
        this.deletedAt = deletedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static Vote reconstitute(
            Long id,
            Long roomId,
            Long sourceCardId,
            Long parentVoteId,
            String title,
            VoteType voteType,
            Integer maxSelectCount,
            Boolean isAnonymous,
            VoteStatus status,
            LocalDateTime deadlineAt,
            VoteCloseReason closeReason,
            LocalDateTime closedAt,
            Long closedByAccountId,
            Long creatorAccountId,
            LocalDateTime reminderSentAt,
            Long version,
            LocalDateTime deletedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new Vote(id, roomId, sourceCardId, parentVoteId, title, voteType, maxSelectCount, isAnonymous, status, deadlineAt, closeReason, closedAt, closedByAccountId, creatorAccountId, reminderSentAt, version, deletedAt, createdAt, updatedAt);
    }
}
