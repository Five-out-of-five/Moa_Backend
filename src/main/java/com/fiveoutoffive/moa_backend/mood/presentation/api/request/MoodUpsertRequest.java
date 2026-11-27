package com.fiveoutoffive.moa_backend.mood.presentation.api.request;

import com.fiveoutoffive.moa_backend.mood.application.command.MoodUpsertCommand;
import com.fiveoutoffive.moa_backend.mood.domain.model.Mood;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MoodUpsertRequest(
        @NotNull Mood mood,
        @Schema(types = {"string", "null"}, maxLength = 50,
                description = "한마디(50자). null 이거나 생략하면 한마디를 지운다. 기분만 바꿀 때도 기존 한마디를 다시 보내야 남는다. 빈 문자열은 빈 문자열로 저장되므로 지울 때는 null 로 보낸다.")
        @Size(max=50) String comment
) {

    public MoodUpsertCommand toCommand() {
        return new MoodUpsertCommand(mood, comment);
    }
}
