package com.fiveoutoffive.moa_backend.task.application.result;

import java.util.List;

public record WorkloadResponse(List<MemberLoad> members, double averageScore, List<Warning> warnings) {

    public record MemberLoad(Long accountId, String displayName, double score, String level, long openTaskCount) {
    }

    public record Warning(Long accountId, String reason) {
    }
}
