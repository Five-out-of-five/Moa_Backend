package com.fiveoutoffive.moa_backend.ideacard.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 아이디어 보드 카드 (안건 / KPT) (idea_card)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdeaCard {

    /** PK */
    private Long id;

    /** 방 (NOT NULL) */
    private Long roomId;

    /** 소속 회차 (필수, 등록 시점의 진행 중 회차. 진행 중 회차가 없으면 등록 불가). 회차는 방과 함께만 삭제 (NOT NULL) */
    private Long sessionId;

    /** 안건 / KEEP / PROBLEM / TRY (NOT NULL) */
    private CardType cardType;

    /** 제목 (NOT NULL) */
    private String title;

    /** 내용 */
    private String content;

    /** 작성자 (NOT NULL) */
    private Long writerAccountId;

    /** 컬럼 내 정렬 순서 (NOT NULL) */
    private Integer sortOrder;

    /** 채택된 AI 절충안 카드 여부 (NOT NULL) */
    private Boolean isAiGenerated;

    /** 낙관적 락 (동시에 같은 카드를 이동하면 나중 요청 거절) (NOT NULL) */
    private Long version;

    /** 마지막 수정 시각 */
    private LocalDateTime editedAt;

    /** 소프트 딜리트 시각 (NULL = 정상) */
    private LocalDateTime deletedAt;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    /** 수정 시각 */
    private LocalDateTime updatedAt;

    private IdeaCard(
            Long id,
            Long roomId,
            Long sessionId,
            CardType cardType,
            String title,
            String content,
            Long writerAccountId,
            Integer sortOrder,
            Boolean isAiGenerated,
            Long version,
            LocalDateTime editedAt,
            LocalDateTime deletedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.roomId = roomId;
        this.sessionId = sessionId;
        this.cardType = cardType;
        this.title = title;
        this.content = content;
        this.writerAccountId = writerAccountId;
        this.sortOrder = sortOrder;
        this.isAiGenerated = isAiGenerated;
        this.version = version;
        this.editedAt = editedAt;
        this.deletedAt = deletedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static IdeaCard reconstitute(
            Long id,
            Long roomId,
            Long sessionId,
            CardType cardType,
            String title,
            String content,
            Long writerAccountId,
            Integer sortOrder,
            Boolean isAiGenerated,
            Long version,
            LocalDateTime editedAt,
            LocalDateTime deletedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new IdeaCard(id, roomId, sessionId, cardType, title, content, writerAccountId, sortOrder, isAiGenerated, version, editedAt, deletedAt, createdAt, updatedAt);
    }
}
