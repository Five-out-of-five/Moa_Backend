package com.fiveoutoffive.moa_backend.account.presentation.api.request;

import com.fiveoutoffive.moa_backend.account.application.command.AiSettingsUpdateCommand;
import com.fiveoutoffive.moa_backend.account.domain.model.ToneMode;
import jakarta.validation.constraints.NotNull;

public record AiSettingsUpdateRequest(
        @NotNull ToneMode toneMode
) {

    public AiSettingsUpdateCommand toCommand() {
        return new AiSettingsUpdateCommand(toneMode);
    }
}
