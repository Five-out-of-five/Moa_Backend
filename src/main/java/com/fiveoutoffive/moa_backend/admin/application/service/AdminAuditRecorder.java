package com.fiveoutoffive.moa_backend.admin.application.service;

import com.fiveoutoffive.moa_backend.admin.domain.model.AdminAuditLog;
import com.fiveoutoffive.moa_backend.admin.domain.repository.AdminAuditLogRepository;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 관리자 변경 조작 감사 로그 (1년 보관). */
@Component
@RequiredArgsConstructor
public class AdminAuditRecorder {

    private final AdminAuditLogRepository repository;
    private final tools.jackson.databind.json.JsonMapper jsonMapper;

    public void record(Long adminId, String actionType, String targetType, Long targetId, Map<String, ?> detail) {
        var attributes = (org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        String ip = attributes == null ? null : attributes.getRequest().getRemoteAddr();
        repository.save(AdminAuditLog.builder().adminAccountId(adminId).actionType(actionType).targetType(targetType).targetId(targetId).detailJson(detail == null ? null : jsonMapper.writeValueAsString(detail)).ipAddress(ip).build());
    }

    @Scheduled(cron = "0 50 19 * * *", zone = "UTC")
    @Transactional
    public void deleteOld() {
        repository.deleteOlderThan(KstTime.nowUtc().minusYears(1));
    }
}
