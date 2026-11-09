package com.fiveoutoffive.moa_backend.vote.presentation.api.request;

import com.fiveoutoffive.moa_backend.vote.application.command.VoteUpdateCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;

public record VoteUpdateRequest(
        @Size(min=1,max=100) String title,
        @Size(max=10) List<@NotBlank@Size(max=50)String> options,
        @Min(1) Integer maxSelectCount,
        OffsetDateTime deadlineAt,
        @NotNull Long version
) {

    public VoteUpdateCommand toCommand() {
        return new VoteUpdateCommand(title, options, maxSelectCount, deadlineAt, version);
    }
}
