package com.fiveoutoffive.moa_backend.vote.presentation.api.request;

import com.fiveoutoffive.moa_backend.vote.application.command.VoteCreateCommand;
import com.fiveoutoffive.moa_backend.vote.domain.model.VoteType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;

public record VoteCreateRequest(
        @NotBlank @Size(max=100) String title,
        @NotNull VoteType voteType,
        @Size(max=10) List<@NotBlank@Size(max=50)String> options,
        @Min(1) Integer maxSelectCount,
        @NotNull OffsetDateTime deadlineAt,
        @NotNull Boolean anonymous,
        Long sourceCardId
) {

    public VoteCreateCommand toCommand() {
        return new VoteCreateCommand(title, voteType, options, maxSelectCount, deadlineAt, anonymous, sourceCardId);
    }
}
