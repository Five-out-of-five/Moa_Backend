package com.fiveoutoffive.moa_backend.admin.presentation.api.request;

import com.fiveoutoffive.moa_backend.admin.application.command.RoomInactivateCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoomInactivateRequest(
        @NotBlank @Size(max=500) String reason
) {

    public RoomInactivateCommand toCommand() {
        return new RoomInactivateCommand(reason);
    }
}
