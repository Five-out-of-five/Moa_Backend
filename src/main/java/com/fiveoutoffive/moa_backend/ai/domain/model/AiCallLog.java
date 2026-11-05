package com.fiveoutoffive.moa_backend.ai.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI 호출 로그 (비용·통계, 지난 DB ai_agent_log 계승) (ai_call_log)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiCallLog {

    /** PK */
    private Long id;

    /** AI 기능 (NOT NULL) */
    private AiFeature feature;

    /** 방 (로그 보존을 위해 FK 미설정) */
    private Long roomId;

    /** 호출한 회원 (FK 미설정) */
    private Long accountId;

    /** 관련 대상 종류 */
    private String refType;

    /** 관련 대상 ID */
    private Long refId;

    /** 사용 모델 */
    private String model;

    /** 입력 토큰 */
    private Integer promptTokens;

    /** 출력 토큰 */
    private Integer outputTokens;

    /** 비용 */
    private BigDecimal costAmount;

    /** 응답 시간(ms) */
    private Integer latencyMs;

    /** 결과 (NOT NULL) */
    private AiCallStatus status;

    /** 오류 메시지 */
    private String errorMessage;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    private AiCallLog(
            Long id,
            AiFeature feature,
            Long roomId,
            Long accountId,
            String refType,
            Long refId,
            String model,
            Integer promptTokens,
            Integer outputTokens,
            BigDecimal costAmount,
            Integer latencyMs,
            AiCallStatus status,
            String errorMessage,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.feature = feature;
        this.roomId = roomId;
        this.accountId = accountId;
        this.refType = refType;
        this.refId = refId;
        this.model = model;
        this.promptTokens = promptTokens;
        this.outputTokens = outputTokens;
        this.costAmount = costAmount;
        this.latencyMs = latencyMs;
        this.status = status;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static AiCallLog reconstitute(
            Long id,
            AiFeature feature,
            Long roomId,
            Long accountId,
            String refType,
            Long refId,
            String model,
            Integer promptTokens,
            Integer outputTokens,
            BigDecimal costAmount,
            Integer latencyMs,
            AiCallStatus status,
            String errorMessage,
            LocalDateTime createdAt
    ) {
        return new AiCallLog(id, feature, roomId, accountId, refType, refId, model, promptTokens, outputTokens, costAmount, latencyMs, status, errorMessage, createdAt);
    }
}
