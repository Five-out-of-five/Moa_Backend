package com.fiveoutoffive.moa_backend.mediation.application.command;

public record DraftAdoptCommand(
        String title,
        String content
) {
}
