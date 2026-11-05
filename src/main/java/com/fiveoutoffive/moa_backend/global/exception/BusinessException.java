package com.fiveoutoffive.moa_backend.global.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final BaseErrorCode errorCode;

    /**
     * 에러 응답의 data 로 함께 내려줄 값. 없으면 null.
     * 예: 버전 충돌 시 최신 상태 응답 DTO, 정지 계정의 정지 종료 시각.
     * 그대로 JSON 으로 나가므로 도메인 모델·JPA 엔티티가 아니라 응답 DTO 를 넣는다.
     */
    private final Object data;

    public BusinessException(BaseErrorCode errorCode) {
        this(errorCode, null);
    }

    public BusinessException(BaseErrorCode errorCode, Object data) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.data = data;
    }
}
