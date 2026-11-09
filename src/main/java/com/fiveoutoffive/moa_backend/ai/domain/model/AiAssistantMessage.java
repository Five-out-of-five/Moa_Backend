package com.fiveoutoffive.moa_backend.ai.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI 협업 어시스턴트 대화 이력 (방 단위 보관) (ai_assistant_message)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiAssistantMessage {

    /** PK */
    private Long id;

    /** 방 (NOT NULL) */
    private Long roomId;

    /** 질문한 멤버 (탈퇴해도 보존) (NOT NULL) */
    private Long accountId;

    /** 질문 (200자 이내) (NOT NULL) */
    private String question;

    /** 답변 (Markdown). 스트리밍 중단 시 부분 응답 */
    private String answer;

    /** 응답 상태 (NOT NULL) */
    private AssistantMessageStatus status;

    /** 답변에 참조한 카드·업무 목록 */
    private String referenceJson;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    private AiAssistantMessage(
            Long id,
            Long roomId,
            Long accountId,
            String question,
            String answer,
            AssistantMessageStatus status,
            String referenceJson,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.roomId = roomId;
        this.accountId = accountId;
        this.question = question;
        this.answer = answer;
        this.status = status;
        this.referenceJson = referenceJson;
        this.createdAt = createdAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static AiAssistantMessage reconstitute(
            Long id,
            Long roomId,
            Long accountId,
            String question,
            String answer,
            AssistantMessageStatus status,
            String referenceJson,
            LocalDateTime createdAt
    ) {
        return new AiAssistantMessage(id, roomId, accountId, question, answer, status, referenceJson, createdAt);
    }
}
