package com.fiveoutoffive.moa_backend.room.application.command;

public record MemberProfileUpdateCommand(
        String nickname,
        Long profileFileId,
        String positionLabel
) {
}
