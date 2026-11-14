package com.fiveoutoffive.moa_backend.vote.presentation.api.request;

import com.fiveoutoffive.moa_backend.vote.application.command.BallotCommand;
import java.util.List;

public record BallotRequest(
        List<Long> optionIds
) {

    public BallotCommand toCommand() {
        return new BallotCommand(optionIds);
    }
}
