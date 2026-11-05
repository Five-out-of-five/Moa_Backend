package com.fiveoutoffive.moa_backend.vote.exception;

import com.fiveoutoffive.moa_backend.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// code 는 API 명세의 에러 코드 문자열 그대로 쓴다 (프론트와의 계약)
@Getter
@RequiredArgsConstructor
public enum VoteErrorCode implements BaseErrorCode {

    VOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "VOTE_NOT_FOUND", "투표를 찾을 수 없습니다."),
    VOTE_NO_PERMISSION(HttpStatus.FORBIDDEN, "VOTE_NO_PERMISSION", "투표 생성자·방장만 할 수 있습니다."),
    VOTE_CLOSED(HttpStatus.CONFLICT, "VOTE_CLOSED", "마감된 투표입니다."),
    VOTE_NOT_CLOSED(HttpStatus.CONFLICT, "VOTE_NOT_CLOSED", "마감된 투표에서만 할 수 있습니다."),
    // 찬반 제외
    VOTE_OPTION_COUNT_INVALID(HttpStatus.BAD_REQUEST, "VOTE_OPTION_COUNT_INVALID", "선택지는 2~10개여야 합니다."),
    VOTE_DEADLINE_TOO_SOON(HttpStatus.BAD_REQUEST, "VOTE_DEADLINE_TOO_SOON", "마감 시각은 현재로부터 1시간 이후여야 합니다."),
    VOTE_MAX_SELECT_INVALID(HttpStatus.BAD_REQUEST, "VOTE_MAX_SELECT_INVALID", "최대 선택 개수는 1 이상 선택지 수 이하여야 합니다."),
    VOTE_HAS_PARTICIPANTS(HttpStatus.CONFLICT, "VOTE_HAS_PARTICIPANTS", "참여자가 있어 마감 시각만 변경할 수 있습니다."),
    VOTE_VERSION_CONFLICT(HttpStatus.CONFLICT, "VOTE_VERSION_CONFLICT", "수정 중에 다른 멤버가 참여해 반영되지 않았습니다."),
    VOTE_SELECTION_INVALID(HttpStatus.BAD_REQUEST, "VOTE_SELECTION_INVALID", "선택지를 1개 이상, 허용 개수 이하로 골라야 합니다."),
    VOTE_NOT_TIED(HttpStatus.CONFLICT, "VOTE_NOT_TIED", "동점이 아닌 투표는 재투표할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
