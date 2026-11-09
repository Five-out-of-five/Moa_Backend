package com.fiveoutoffive.moa_backend.admin.application.result;

import java.time.LocalDateTime;

public record AuditLogResponse(Long auditLogId, Admin admin, String actionType, String targetType, Long targetId, String detail, String ipAddress, LocalDateTime createdAt) {

    public record Admin(Long accountId, String name) {
    }
}
