package com.fiveoutoffive.moa_backend.mood.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 멤버 상태값 (방별, 일자별 1건) (mood_status)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoodStatus {

    /** PK */
    private Long id;

    /** 방 (NOT NULL) */
    private Long roomId;

    /** 멤버 (NOT NULL) */
    private Long accountId;

    /** 아주 좋아요 / 좋아요 / 보통이에요 / 지쳤어요 / 힘들어요 (NOT NULL) */
    private Mood mood;

    /** 한 줄 코멘트 */
    private String comment;

    /** 기록 일자 (같은 날 재등록 시 갱신) (NOT NULL) */
    private LocalDate recordedDate;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    /** 수정 시각 */
    private LocalDateTime updatedAt;

    private MoodStatus(
            Long id,
            Long roomId,
            Long accountId,
            Mood mood,
            String comment,
            LocalDate recordedDate,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.roomId = roomId;
        this.accountId = accountId;
        this.mood = mood;
        this.comment = comment;
        this.recordedDate = recordedDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static MoodStatus reconstitute(
            Long id,
            Long roomId,
            Long accountId,
            Mood mood,
            String comment,
            LocalDate recordedDate,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new MoodStatus(id, roomId, accountId, mood, comment, recordedDate, createdAt, updatedAt);
    }
}
