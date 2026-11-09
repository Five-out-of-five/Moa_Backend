package com.fiveoutoffive.moa_backend.ai.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카드·업무·투표 임베딩 (pgvector) (content_embedding)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentEmbedding {

    /** PK */
    private Long id;

    /** 방 (검색은 항상 방 단위) (NOT NULL) */
    private Long roomId;

    /** 대상 카드 */
    private Long cardId;

    /** 대상 업무 */
    private Long taskId;

    /** 대상 투표 */
    private Long voteId;

    /** 임베딩 벡터 (차원은 사용 모델에 맞춰 조정) (NOT NULL) */
    private float[] embedding;

    /** 임베딩 모델 (NOT NULL) */
    private String model;

    /** 원문 해시 (내용이 바뀐 경우에만 재생성) (NOT NULL) */
    private String sourceHash;

    /** 갱신 시각 (NOT NULL) */
    private LocalDateTime updatedAt;

    private ContentEmbedding(
            Long id,
            Long roomId,
            Long cardId,
            Long taskId,
            Long voteId,
            float[] embedding,
            String model,
            String sourceHash,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.roomId = roomId;
        this.cardId = cardId;
        this.taskId = taskId;
        this.voteId = voteId;
        this.embedding = embedding;
        this.model = model;
        this.sourceHash = sourceHash;
        this.updatedAt = updatedAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static ContentEmbedding reconstitute(
            Long id,
            Long roomId,
            Long cardId,
            Long taskId,
            Long voteId,
            float[] embedding,
            String model,
            String sourceHash,
            LocalDateTime updatedAt
    ) {
        return new ContentEmbedding(id, roomId, cardId, taskId, voteId, embedding, model, sourceHash, updatedAt);
    }
}
