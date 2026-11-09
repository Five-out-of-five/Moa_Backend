package com.fiveoutoffive.moa_backend.room.application.result;

public record LeaveCheckResponse(boolean leavable, boolean isOwner, long assignedOpenTaskCount) {
}
