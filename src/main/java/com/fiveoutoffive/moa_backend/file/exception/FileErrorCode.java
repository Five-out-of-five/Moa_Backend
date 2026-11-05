package com.fiveoutoffive.moa_backend.file.exception;

import com.fiveoutoffive.moa_backend.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// code 는 API 명세의 에러 코드 문자열 그대로 쓴다 (프론트와의 계약)
@Getter
@RequiredArgsConstructor
public enum FileErrorCode implements BaseErrorCode {

    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE_NOT_FOUND", "파일을 찾을 수 없습니다."),
    FILE_TYPE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "FILE_TYPE_NOT_ALLOWED", "허용되지 않은 파일 형식입니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.CONTENT_TOO_LARGE, "FILE_SIZE_EXCEEDED", "파일 크기 제한을 넘었습니다."),
    FILE_STORAGE_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "FILE_STORAGE_LIMIT_EXCEEDED", "방 저장 용량(2GB)을 넘어 업로드할 수 없습니다."),
    FILE_ATTACHMENT_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "FILE_ATTACHMENT_LIMIT_EXCEEDED", "첨부 개수 제한(5개)을 넘었습니다."),
    FILE_ALREADY_ATTACHED(HttpStatus.CONFLICT, "FILE_ALREADY_ATTACHED", "이미 다른 곳에 첨부된 파일입니다."),
    FILE_NOT_OWNER(HttpStatus.FORBIDDEN, "FILE_NOT_OWNER", "본인이 업로드한 파일만 사용할 수 있습니다."),
    FILE_PURPOSE_MISMATCH(HttpStatus.BAD_REQUEST, "FILE_PURPOSE_MISMATCH", "파일 용도가 맞지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
