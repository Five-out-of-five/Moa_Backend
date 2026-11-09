package com.fiveoutoffive.moa_backend.vote.application.command;

import com.fiveoutoffive.moa_backend.vote.domain.model.VoteType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;

public record VoteCreateCommand(
        String title,
        VoteType voteType,
        List<@NotBlank@Size(max=50)String> options,
        Integer maxSelectCount,
        OffsetDateTime deadlineAt,
        Boolean anonymous,
        Long sourceCardId
) {
}
