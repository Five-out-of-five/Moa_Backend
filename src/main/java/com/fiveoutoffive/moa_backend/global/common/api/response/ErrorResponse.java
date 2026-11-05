package com.fiveoutoffive.moa_backend.global.common.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "공통 에러 응답 데이터")
public record ErrorResponse(
        @Schema(description = "에러 발생 시각", example = "2026-05-21T07:09:00Z")
        Instant timestamp,

        @Schema(description = "HTTP 상태 코드", example = "404")
        int status,

        @Schema(description = "에러 분류 코드", example = "EX_001")
        String errorCode,

        @Schema(description = "에러 상세 메시지", example = "예시 데이터를 찾을 수 없습니다.")
        String message,

        @Schema(description = "에러 추적 ID (로그 확인용)", example = "a1b2c3d4")
        String traceId,

        // 값이 없으면 응답에서 아예 빠진다 (기존 응답 모양 유지)
        @Schema(description = "필드별 검증 실패 사유 (@Valid 실패 시에만)")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        List<FieldErrorDetail> errors,

        @Schema(description = "에러와 함께 내려주는 추가 데이터 (예: 버전 충돌 시 최신 상태, 정지 종료 시각)")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Object data
) {

    /** errors·data 없이 만드는 기존 형태. */
    public ErrorResponse(Instant timestamp, int status, String errorCode, String message, String traceId) {
        this(timestamp, status, errorCode, message, traceId, null, null);
    }

    @Schema(description = "필드별 검증 실패 사유")
    public record FieldErrorDetail(
            @Schema(description = "요청 필드 이름", example = "title")
            String field,

            @Schema(description = "실패 사유", example = "제목은 필수입니다.")
            String reason
    ) {}
}
