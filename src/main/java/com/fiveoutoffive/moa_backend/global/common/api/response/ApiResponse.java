package com.fiveoutoffive.moa_backend.global.common.api.response;

/*
 * ApiResponse는 성공 응답의 공통 표현 형식이다. (API 명세 공통 규칙 "응답 형식")
 * { success: true, data: ... } 이며, 표의 '없음'은 data=null(HTTP 200)이다.
 */
public record ApiResponse<T>(
        boolean success,
        T data
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data);
    }

    public static ApiResponse<Void> empty() {
        return new ApiResponse<>(true, null);
    }
}
