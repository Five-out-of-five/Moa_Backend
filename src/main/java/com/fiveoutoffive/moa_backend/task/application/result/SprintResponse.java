package com.fiveoutoffive.moa_backend.task.application.result;

import com.fiveoutoffive.moa_backend.global.common.api.response.MemberRef;
import com.fiveoutoffive.moa_backend.task.domain.model.SprintStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SprintResponse(Long sprintId, Integer sprintNo, String name, LocalDate startDate, LocalDate endDate, SprintStatus status, MemberRef startedBy, LocalDateTime closedAt, Integer totalTaskCount, Integer doneTaskCount, Integer completionRate) {
}
