package com.fiveoutoffive.moa_backend.room.presentation.api.request;

import com.fiveoutoffive.moa_backend.room.application.command.MemberProfileUpdateCommand;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MemberProfileUpdateRequest(
        @Size(min=2,max=20) @Pattern(regexp="^[\\p{L}\\p{N} ]+$",message="특수문자는 사용할 수 없습니다.") String nickname,
        Long profileFileId,
        @Size(max=20) String positionLabel
) {

    public MemberProfileUpdateCommand toCommand() {
        return new MemberProfileUpdateCommand(nickname, profileFileId, positionLabel);
    }
}
