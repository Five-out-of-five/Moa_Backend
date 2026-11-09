package com.fiveoutoffive.moa_backend.vote.application.command;

import java.util.List;

public record BallotCommand(
        List<Long> optionIds
) {
}
