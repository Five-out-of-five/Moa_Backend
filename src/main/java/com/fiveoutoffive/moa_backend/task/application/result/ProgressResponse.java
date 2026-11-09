package com.fiveoutoffive.moa_backend.task.application.result;

import com.fiveoutoffive.moa_backend.task.domain.model.TaskStatus;
import java.time.LocalDate;
import java.util.Map;

public record ProgressResponse(SprintInfo sprint, long totalCount, long doneCount, int progressRate, Map<TaskStatus, Long> statusCounts, long dueSoonCount) {

    public record SprintInfo(Long sprintId, Integer sprintNo, String name, LocalDate startDate, LocalDate endDate) {
    }
}
