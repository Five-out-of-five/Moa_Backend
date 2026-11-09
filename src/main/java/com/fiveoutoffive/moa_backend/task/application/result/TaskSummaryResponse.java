package com.fiveoutoffive.moa_backend.task.application.result;

import com.fiveoutoffive.moa_backend.global.common.api.response.MemberRef;
import com.fiveoutoffive.moa_backend.task.domain.model.TaskPriority;
import com.fiveoutoffive.moa_backend.task.domain.model.TaskSourceType;
import com.fiveoutoffive.moa_backend.task.domain.model.TaskStatus;
import java.time.LocalDate;

public record TaskSummaryResponse(Long taskId, String title, TaskStatus status, TaskPriority priority, LocalDate dueDate, Long dDay, boolean overdue, Integer sortOrder, Long version, MemberRef registrant, MemberRef assignee, Source source, Long sprintId, long fileCount) {

    public record Source(TaskSourceType type, Long id, String title, String writerName, boolean deleted) {
    }
}
