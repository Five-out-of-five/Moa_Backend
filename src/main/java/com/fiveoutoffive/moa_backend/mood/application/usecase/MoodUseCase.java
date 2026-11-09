package com.fiveoutoffive.moa_backend.mood.application.usecase;

import com.fiveoutoffive.moa_backend.mood.application.command.MoodUpsertCommand;
import com.fiveoutoffive.moa_backend.mood.application.result.MoodResponse;
import com.fiveoutoffive.moa_backend.mood.application.result.TeamMoodResponse;

public interface MoodUseCase {

    MoodResponse upsertMine(Long accountId, Long roomId, MoodUpsertCommand command);

    TeamMoodResponse getTeam(Long accountId, Long roomId, String period);
}
