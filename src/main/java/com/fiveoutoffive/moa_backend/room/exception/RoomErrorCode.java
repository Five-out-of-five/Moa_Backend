package com.fiveoutoffive.moa_backend.room.exception;

import com.fiveoutoffive.moa_backend.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// code 는 API 명세의 에러 코드 문자열 그대로 쓴다 (프론트와의 계약)
@Getter
@RequiredArgsConstructor
public enum RoomErrorCode implements BaseErrorCode {

    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "ROOM_NOT_FOUND", "방을 찾을 수 없습니다."),
    ROOM_NOT_MEMBER(HttpStatus.FORBIDDEN, "ROOM_NOT_MEMBER", "방 멤버가 아닙니다."),
    ROOM_INACTIVE(HttpStatus.FORBIDDEN, "ROOM_INACTIVE", "비활성화된 방입니다."),
    ROOM_OWNER_ONLY(HttpStatus.FORBIDDEN, "ROOM_OWNER_ONLY", "방장만 할 수 있습니다."),
    ROOM_CREATE_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "ROOM_CREATE_LIMIT_EXCEEDED", "개설할 수 있는 방 수(10개)를 넘었습니다."),
    ROOM_DELETE_CONFIRM_MISMATCH(HttpStatus.BAD_REQUEST, "ROOM_DELETE_CONFIRM_MISMATCH", "입력한 방 이름이 일치하지 않습니다."),
    ROOM_MEMBER_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "ROOM_MEMBER_LIMIT_EXCEEDED", "방 최대 인원(20명)에 도달했습니다."),
    INVITE_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "INVITE_CODE_NOT_FOUND", "유효하지 않은 초대 링크입니다."),
    // roomId 를 함께 반환
    JOIN_ALREADY_MEMBER(HttpStatus.CONFLICT, "JOIN_ALREADY_MEMBER", "이미 참여 중인 방입니다."),
    JOIN_KICKED_MEMBER(HttpStatus.FORBIDDEN, "JOIN_KICKED_MEMBER", "내보내진 방에는 다시 참여할 수 없습니다."),
    JOIN_REQUEST_ALREADY_PENDING(HttpStatus.CONFLICT, "JOIN_REQUEST_ALREADY_PENDING", "이미 대기 중인 참여 요청이 있습니다."),
    JOIN_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "JOIN_REQUEST_NOT_FOUND", "참여 요청을 찾을 수 없습니다."),
    JOIN_REQUEST_ALREADY_PROCESSED(HttpStatus.CONFLICT, "JOIN_REQUEST_ALREADY_PROCESSED", "이미 처리되었거나 만료된 요청입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "방 멤버를 찾을 수 없습니다."),
    MEMBER_CANNOT_KICK_SELF(HttpStatus.BAD_REQUEST, "MEMBER_CANNOT_KICK_SELF", "본인은 내보낼 수 없습니다."),
    MEMBER_OWNER_CANNOT_LEAVE(HttpStatus.CONFLICT, "MEMBER_OWNER_CANNOT_LEAVE", "방장은 위임하기 전에는 방을 나갈 수 없습니다."),
    MEMBER_NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "MEMBER_NICKNAME_DUPLICATED", "이 방에서 이미 사용 중인 닉네임입니다."),
    // 본인·비활성 멤버
    MEMBER_DELEGATE_TARGET_INVALID(HttpStatus.BAD_REQUEST, "MEMBER_DELEGATE_TARGET_INVALID", "위임 대상이 올바르지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
