package com.fiveoutoffive.moa_backend.chat.exception;

import com.fiveoutoffive.moa_backend.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// code 는 API 명세의 에러 코드 문자열 그대로 쓴다 (프론트와의 계약)
@Getter
@RequiredArgsConstructor
public enum ChatErrorCode implements BaseErrorCode {

    // 참여하지 않은 비공개 채널 포함
    CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "CHANNEL_NOT_FOUND", "채널을 찾을 수 없습니다."),
    CHANNEL_NAME_DUPLICATED(HttpStatus.CONFLICT, "CHANNEL_NAME_DUPLICATED", "같은 이름의 채널이 있습니다."),
    CHANNEL_NO_PERMISSION(HttpStatus.FORBIDDEN, "CHANNEL_NO_PERMISSION", "채널 생성자·방장만 할 수 있습니다."),
    CHANNEL_DEFAULT_IMMUTABLE(HttpStatus.CONFLICT, "CHANNEL_DEFAULT_IMMUTABLE", "기본 채널(전체)은 변경·삭제·나가기를 할 수 없습니다."),
    CHANNEL_NOT_JOINED(HttpStatus.FORBIDDEN, "CHANNEL_NOT_JOINED", "참여 중인 채널이 아닙니다."),
    CHANNEL_DELETE_CONFIRM_MISMATCH(HttpStatus.BAD_REQUEST, "CHANNEL_DELETE_CONFIRM_MISMATCH", "입력한 채널 이름이 일치하지 않습니다."),
    CHANNEL_MEMBER_ALREADY_JOINED(HttpStatus.CONFLICT, "CHANNEL_MEMBER_ALREADY_JOINED", "이미 참여 중인 멤버입니다."),
    CHANNEL_CANNOT_KICK_CREATOR(HttpStatus.CONFLICT, "CHANNEL_CANNOT_KICK_CREATOR", "채널 생성자는 내보낼 수 없습니다."),
    CHANNEL_INVITATION_NOT_FOUND(HttpStatus.NOT_FOUND, "CHANNEL_INVITATION_NOT_FOUND", "받은 초대가 없습니다."),
    CHANNEL_DM_TARGET_INVALID(HttpStatus.BAD_REQUEST, "CHANNEL_DM_TARGET_INVALID", "같은 방의 다른 멤버와만 1:1 대화를 시작할 수 있습니다."),
    CHANNEL_NOT_DIRECT(HttpStatus.BAD_REQUEST, "CHANNEL_NOT_DIRECT", "1:1 대화에서만 할 수 있습니다."),
    // 조회만 가능
    CHANNEL_DM_PEER_LEFT(HttpStatus.CONFLICT, "CHANNEL_DM_PEER_LEFT", "상대가 방을 나가 메시지를 보낼 수 없습니다."),
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "MESSAGE_NOT_FOUND", "메시지를 찾을 수 없습니다."),
    MESSAGE_NOT_SENDER(HttpStatus.FORBIDDEN, "MESSAGE_NOT_SENDER", "본인 메시지만 수정·삭제할 수 있습니다."),
    MESSAGE_EMPTY(HttpStatus.BAD_REQUEST, "MESSAGE_EMPTY", "내용이나 첨부 파일 중 하나는 있어야 합니다."),
    MESSAGE_ALREADY_DELETED(HttpStatus.CONFLICT, "MESSAGE_ALREADY_DELETED", "이미 삭제된 메시지입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
