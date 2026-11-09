package com.fiveoutoffive.moa_backend.account.application.command;

import com.fiveoutoffive.moa_backend.account.domain.model.ToneMode;

public record AiSettingsUpdateCommand(
        ToneMode toneMode
) {
}
