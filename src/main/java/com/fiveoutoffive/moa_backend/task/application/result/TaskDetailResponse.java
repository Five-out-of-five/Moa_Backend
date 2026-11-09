package com.fiveoutoffive.moa_backend.task.application.result;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fiveoutoffive.moa_backend.file.application.result.FileResponse;
import com.fiveoutoffive.moa_backend.global.common.api.response.MemberRef;
import com.fiveoutoffive.moa_backend.task.domain.model.TaskHistoryType;
import java.time.LocalDateTime;
import java.util.List;

public record TaskDetailResponse(@JsonUnwrapped TaskSummaryResponse summary, String description, LocalDateTime completedAt, List<FileResponse> files, List<History> history, LocalDateTime createdAt) {

    public record History(TaskHistoryType historyType, String fromStatus, String toStatus, MemberRef fromAssignee, MemberRef toAssignee, MemberRef actor, LocalDateTime createdAt) {
    }
}
