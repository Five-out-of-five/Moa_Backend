package com.fiveoutoffive.moa_backend.task.domain.event;

public record TaskDueSoonEvent(Long roomId, Long taskId, String title) {
}
