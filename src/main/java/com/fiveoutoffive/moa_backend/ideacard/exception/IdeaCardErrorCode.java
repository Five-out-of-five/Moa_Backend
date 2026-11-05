package com.fiveoutoffive.moa_backend.ideacard.exception;

import com.fiveoutoffive.moa_backend.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// code 는 API 명세의 에러 코드 문자열 그대로 쓴다 (프론트와의 계약)
@Getter
@RequiredArgsConstructor
public enum IdeaCardErrorCode implements BaseErrorCode {

    // 삭제된 카드 포함
    CARD_NOT_FOUND(HttpStatus.NOT_FOUND, "CARD_NOT_FOUND", "카드를 찾을 수 없습니다."),
    CARD_NOT_WRITER(HttpStatus.FORBIDDEN, "CARD_NOT_WRITER", "작성자만 할 수 있습니다."),
    CARD_SESSION_CLOSED(HttpStatus.CONFLICT, "CARD_SESSION_CLOSED", "마감된 회차의 카드는 변경할 수 없습니다."),
    // 최신 상태를 함께 반환
    CARD_VERSION_CONFLICT(HttpStatus.CONFLICT, "CARD_VERSION_CONFLICT", "다른 멤버가 먼저 변경했습니다."),
    CARD_LOCKED(HttpStatus.CONFLICT, "CARD_LOCKED", "다른 멤버가 편집 중입니다."),
    CARD_NOT_AGENDA(HttpStatus.BAD_REQUEST, "CARD_NOT_AGENDA", "안건 카드에서만 투표를 만들 수 있습니다."),
    CARD_ALREADY_CONVERTED(HttpStatus.CONFLICT, "CARD_ALREADY_CONVERTED", "이미 업무로 전환된 카드입니다."),
    CARD_VOTE_ALREADY_OPEN(HttpStatus.CONFLICT, "CARD_VOTE_ALREADY_OPEN", "이 안건에 진행 중인 투표가 이미 있습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND", "댓글을 찾을 수 없습니다."),
    COMMENT_NOT_WRITER(HttpStatus.FORBIDDEN, "COMMENT_NOT_WRITER", "본인 댓글만 수정·삭제할 수 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
