package com.fiveoutoffive.moa_backend.global.common.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** 실패 응답 형식 (API 명세 공통 규칙 "응답 형식"): { success: false, error: { code, message, errors? } } */
@Schema(description = "공통 에러 응답 데이터")
public record ErrorResponse(
        @Schema(description = "성공 여부. 실패 응답은 항상 false", example = "false")
        boolean success,

        @Schema(description = "에러 내용")
        ErrorBody error
) {

    public static ErrorResponse of(String code, String message, String traceId,
                                   List<FieldErrorDetail> errors, Object data) {
        return new ErrorResponse(false, new ErrorBody(code, message, traceId, errors, data));
    }

    @Schema(description = "에러 내용")
    public record ErrorBody(
            @Schema(description = "에러 코드", example = "ROOM_NOT_FOUND")
            String code,

            @Schema(description = "에러 메시지", example = "방을 찾을 수 없습니다.")
            String message,

            @Schema(description = "에러 추적 ID (로그 확인용)", example = "a1b2c3d4")
            String traceId,

            @Schema(description = "필드별 검증 실패 사유 (@Valid 실패 시에만)")
            @JsonInclude(JsonInclude.Include.NON_NULL)
            List<FieldErrorDetail> errors,

            @Schema(description = "에러와 함께 내려주는 추가 데이터 (예: 버전 충돌 시 최신 상태, 정지 종료 시각)")
            @JsonInclude(JsonInclude.Include.NON_NULL)
            Object data
    ) {}

    @Schema(description = "필드별 검증 실패 사유")
    public record FieldErrorDetail(
            @Schema(description = "요청 필드 이름", example = "title")
            String field,

            @Schema(description = "실패 사유", example = "제목은 필수입니다.")
            String reason
    ) {}
}
