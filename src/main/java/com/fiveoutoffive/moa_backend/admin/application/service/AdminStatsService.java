package com.fiveoutoffive.moa_backend.admin.application.service;

import com.fiveoutoffive.moa_backend.account.domain.model.Account;
import com.fiveoutoffive.moa_backend.account.domain.repository.AccountDailyActivityRepository;
import com.fiveoutoffive.moa_backend.account.domain.repository.AccountRepository;
import com.fiveoutoffive.moa_backend.admin.application.result.AiUsageStatsResponse;
import com.fiveoutoffive.moa_backend.admin.application.result.AuditLogResponse;
import com.fiveoutoffive.moa_backend.admin.application.result.StatsOverviewResponse;
import com.fiveoutoffive.moa_backend.admin.application.usecase.AdminStatsUseCase;
import com.fiveoutoffive.moa_backend.admin.domain.model.AdminAuditLog;
import com.fiveoutoffive.moa_backend.admin.domain.repository.AdminAuditLogRepository;
import com.fiveoutoffive.moa_backend.ai.domain.model.AiCallLog;
import com.fiveoutoffive.moa_backend.ai.domain.model.AiCallStatus;
import com.fiveoutoffive.moa_backend.ai.domain.model.AiFeature;
import com.fiveoutoffive.moa_backend.ai.domain.repository.AiCallLogRepository;
import com.fiveoutoffive.moa_backend.chat.domain.repository.ChatMessageRepository;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.global.util.Pages;
import com.fiveoutoffive.moa_backend.room.domain.repository.RoomRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 집계값만 제공한다. 개인을 식별할 수 있는 정보는 내려주지 않는다. */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminStatsService implements AdminStatsUseCase {

    private final AccountRepository accountRepository;
    private final AccountDailyActivityRepository activityRepository;
    private final RoomRepository roomRepository;
    private final ChatMessageRepository messageRepository;
    private final AiCallLogRepository callLogRepository;
    private final AdminAuditLogRepository auditLogRepository;

    @Value("${app.ai.cost-threshold}")
    private BigDecimal costThreshold;

    @Override
    public StatsOverviewResponse getOverview(String unit, LocalDate from, LocalDate to) {
        List<StatsOverviewResponse.Point> series = new ArrayList<>();
        for (LocalDate start = from; !start.isAfter(to); start = next(start, unit)) {
            LocalDate last = next(start, unit).minusDays(1);
            LocalDateTime fromUtc = KstTime.startOfDayUtc(start);
            LocalDateTime toUtc = KstTime.startOfDayUtc(last.plusDays(1));
            series.add(new StatsOverviewResponse.Point(start, accountRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(fromUtc, toUtc), activityRepository.countBetween(start, last),
                    roomRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(fromUtc, toUtc), messageRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(fromUtc, toUtc)));
        }
        return new StatsOverviewResponse(new StatsOverviewResponse.Totals(accountRepository.countByCreatedAtBefore(KstTime.nowUtc().plusDays(1)), roomRepository.countByDeletedAtIsNull(), messageRepository.countByDeletedAtIsNull()), series, KstTime.nowUtc());
    }

    @Override
    public AiUsageStatsResponse getAiUsage(String unit, LocalDate from, LocalDate to, AiFeature feature) {
        List<AiCallLog> logs = callLogRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(KstTime.startOfDayUtc(from), KstTime.startOfDayUtc(to.plusDays(1))).stream().filter(l -> feature == null || l.getFeature() == feature).toList();
        List<AiUsageStatsResponse.ByFeature> byFeature = logs.stream().collect(Collectors.groupingBy(AiCallLog::getFeature)).entrySet().stream().map(e -> new AiUsageStatsResponse.ByFeature(e.getKey(), e.getValue().size(),
                e.getValue().stream().filter(l -> l.getStatus() != AiCallStatus.SUCCESS).count(), e.getValue().stream().mapToLong(l -> l.getPromptTokens() == null ? 0 : l.getPromptTokens()).sum(),
                e.getValue().stream().mapToLong(l -> l.getOutputTokens() == null ? 0 : l.getOutputTokens()).sum(), sum(e.getValue()))).toList();
        List<AiUsageStatsResponse.Point> series = new ArrayList<>();
        for (LocalDate start = from; !start.isAfter(to); start = next(start, unit)) {
            LocalDateTime fromUtc = KstTime.startOfDayUtc(start);
            LocalDateTime toUtc = KstTime.startOfDayUtc(next(start, unit));
            LocalDate period = start;
            logs.stream().filter(l -> !l.getCreatedAt().isBefore(fromUtc) && l.getCreatedAt().isBefore(toUtc)).collect(Collectors.groupingBy(AiCallLog::getFeature))
                    .forEach((key, value) -> series.add(new AiUsageStatsResponse.Point(period, key, value.size(), sum(value))));
        }
        BigDecimal total = sum(logs);
        return new AiUsageStatsResponse(byFeature, series, new AiUsageStatsResponse.Threshold(costThreshold, total.compareTo(costThreshold) > 0));
    }

    @Override
    public PageResponse<AuditLogResponse> getAuditLogs(Long adminId, String actionType, String targetType, LocalDate from, LocalDate to, Pageable pageable) {
        Page<AdminAuditLog> page = auditLogRepository.search(adminId, actionType, targetType, from == null ? null : KstTime.startOfDayUtc(from), to == null ? null : KstTime.startOfDayUtc(to.plusDays(1)), pageable);
        Map<Long, String> names = accountRepository.findByIdIn(page.stream().map(AdminAuditLog::getAdminAccountId).collect(Collectors.toSet())).stream().collect(Collectors.toMap(Account::getId, a -> String.valueOf(a.getName())));
        return Pages.response(page.map(l -> new AuditLogResponse(l.getId(), new AuditLogResponse.Admin(l.getAdminAccountId(), names.get(l.getAdminAccountId())), l.getActionType(), l.getTargetType(), l.getTargetId(), l.getDetailJson(), l.getIpAddress(), l.getCreatedAt())));
    }

    private LocalDate next(LocalDate start, String unit) {
        return switch (unit) {
            case "WEEK" -> start.plusWeeks(1);
            case "MONTH" -> start.plusMonths(1);
            default -> start.plusDays(1);
        };
    }

    private BigDecimal sum(List<AiCallLog> logs) {
        return logs.stream().map(AiCallLog::getCostAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
