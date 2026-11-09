package com.fiveoutoffive.moa_backend.vote.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;

public record VoteUpdateCommand(
        String title,
        List<@NotBlank@Size(max=50)String> options,
        Integer maxSelectCount,
        OffsetDateTime deadlineAt,
        Long version
) {
}
