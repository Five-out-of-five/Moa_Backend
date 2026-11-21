package com.fiveoutoffive.moa_backend.room.application.command;

import java.util.Optional;

/** 각 필드: null = 보내지 않음(유지), Optional.empty() = 지움, 값 = 변경. */
public record MemberProfileUpdateCommand(
        Optional<String> nickname,
        Optional<Long> profileFileId,
        Optional<String> positionLabel
) {
}
