package com.fiveoutoffive.moa_backend.admin.application.service;

import com.fiveoutoffive.moa_backend.account.domain.model.Account;
import com.fiveoutoffive.moa_backend.account.domain.repository.AccountRepository;
import com.fiveoutoffive.moa_backend.account.exception.AccountErrorCode;
import com.fiveoutoffive.moa_backend.admin.application.command.ReportRejectCommand;
import com.fiveoutoffive.moa_backend.admin.application.command.ReportResolveCommand;
import com.fiveoutoffive.moa_backend.admin.application.result.AdminReportDetailResponse;
import com.fiveoutoffive.moa_backend.admin.application.result.AdminReportResponse;
import com.fiveoutoffive.moa_backend.admin.application.usecase.AdminReportUseCase;
import com.fiveoutoffive.moa_backend.admin.exception.AdminErrorCode;
import com.fiveoutoffive.moa_backend.chat.domain.repository.ChatMessageRepository;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.exception.GlobalErrorCode;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.global.util.Pages;
import com.fiveoutoffive.moa_backend.ideacard.domain.event.CardDeletedEvent;
import com.fiveoutoffive.moa_backend.ideacard.domain.repository.CardCommentRepository;
import com.fiveoutoffive.moa_backend.ideacard.domain.repository.IdeaCardRepository;
import com.fiveoutoffive.moa_backend.notification.application.service.NotificationSender;
import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationType;
import com.fiveoutoffive.moa_backend.report.domain.model.Report;
import com.fiveoutoffive.moa_backend.report.domain.model.ReportAction;
import com.fiveoutoffive.moa_backend.report.domain.model.ReportReason;
import com.fiveoutoffive.moa_backend.report.domain.model.ReportStatus;
import com.fiveoutoffive.moa_backend.report.domain.model.ReportTargetType;
import com.fiveoutoffive.moa_backend.report.domain.repository.ReportRepository;
import com.fiveoutoffive.moa_backend.report.exception.ReportErrorCode;
import com.fiveoutoffive.moa_backend.room.domain.repository.RoomRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminReportService implements AdminReportUseCase {

    private static final int OVERDUE_DAYS = 7;

    private final ReportRepository reportRepository;
    private final AccountRepository accountRepository;
    private final RoomRepository roomRepository;
    private final IdeaCardRepository cardRepository;
    private final CardCommentRepository commentRepository;
    private final ChatMessageRepository messageRepository;
    private final AdminSuspensionSupport suspensionSupport;
    private final AdminAuditRecorder audit;
    private final NotificationSender notificationSender;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.report.false-report-threshold}")
    private int falseThreshold;
    @Value("${app.report.false-report-window-days}")
    private int falseWindowDays;
    @Value("${app.report.block-days}")
    private int blockDays;

    @Transactional(readOnly = true)
    @Override
    public PageResponse<AdminReportResponse> getReports(ReportStatus status, ReportTargetType targetType, ReportReason reason, Pageable pageable) {
        Page<AdminReportResponse> page = reportRepository.search(status, targetType, reason, pageable).map(this::toResponse);
        // 접수 후 7일이 지난 미처리 건을 맨 위에 고정한다.
        List<AdminReportResponse> sorted = page.getContent().stream().sorted(Comparator.comparing(AdminReportResponse::overdue).reversed()).toList();
        return Pages.response(new PageImpl<>(sorted, pageable, page.getTotalElements()));
    }

    @Transactional(readOnly = true)
    @Override
    public AdminReportDetailResponse getReport(Long reportId) {
        return detail(require(reportId));
    }

    @Override
    public AdminReportDetailResponse startReview(Long adminId, Long reportId) {
        Report report = unprocessed(reportId);
        audit.record(adminId, "REPORT_REVIEW", "REPORT", reportId, null);
        return detail(reportRepository.save(report.toBuilder().status(ReportStatus.IN_PROGRESS).build()));
    }

    @Override
    public AdminReportDetailResponse resolve(Long adminId, Long reportId, ReportResolveCommand command) {
        Report report = unprocessed(reportId);
        if (command.action() == ReportAction.SUSPEND_ACCOUNT) {
            if (command.suspensionPeriod() == null) {
                throw new BusinessException(GlobalErrorCode.INVALID_REQUEST);
            }
            Account target = accountRepository.findById(report.getTargetAccountId()).orElseThrow(() -> new BusinessException(AccountErrorCode.ACCOUNT_NOT_FOUND));
            suspensionSupport.suspend(adminId, target, command.suspensionPeriod(), command.actionReason(), reportId);
        } else if (command.action() == ReportAction.DELETE_CONTENT) {
            deleteContent(report);
        }
        Report saved = finish(adminId, report, ReportStatus.RESOLVED, command.action(), command.actionReason(), command.falseReport());
        notificationSender.send(null, null, NotificationType.REPORT_PROCESSED, List.of(report.getReporterAccountId(), report.getTargetAccountId()), "신고 처리 결과가 나왔습니다", command.actionReason(), "REPORT", reportId);
        audit.record(adminId, "REPORT_RESOLVE", "REPORT", reportId, Map.of("action", command.action()));
        return detail(saved);
    }

    @Override
    public AdminReportDetailResponse reject(Long adminId, Long reportId, ReportRejectCommand command) {
        Report report = unprocessed(reportId);
        Report saved = finish(adminId, report, ReportStatus.REJECTED, ReportAction.NONE, command.actionReason(), command.falseReport());
        notificationSender.send(null, null, NotificationType.REPORT_PROCESSED, List.of(report.getReporterAccountId()), "신고가 반려되었습니다", command.actionReason(), "REPORT", reportId);
        audit.record(adminId, "REPORT_REJECT", "REPORT", reportId, null);
        return detail(saved);
    }

    private Report finish(Long adminId, Report report, ReportStatus status, ReportAction action, String reason, Boolean falseReport) {
        LocalDateTime now = KstTime.nowUtc();
        Report saved = reportRepository.save(report.toBuilder().status(status).action(action).actionReason(reason).isFalseReport(Boolean.TRUE.equals(falseReport)).handledByAccountId(adminId).handledAt(now).build());
        if (Boolean.TRUE.equals(falseReport) && reportRepository.countFalseReports(report.getReporterAccountId(), now.minusDays(falseWindowDays)) >= falseThreshold) {
            accountRepository.findById(report.getReporterAccountId()).ifPresent(a -> accountRepository.save(a.toBuilder().reportBlockedUntil(now.plusDays(blockDays)).build()));
        }
        return saved;
    }

    private void deleteContent(Report report) {
        LocalDateTime now = KstTime.nowUtc();
        switch (report.getTargetType()) {
            case CARD -> cardRepository.findById(report.getTargetId()).ifPresent(card -> {
                cardRepository.save(card.toBuilder().deletedAt(now).build());
                eventPublisher.publishEvent(new CardDeletedEvent(card.getRoomId(), card.getId()));
            });
            case COMMENT -> commentRepository.findById(report.getTargetId()).ifPresent(c -> commentRepository.save(c.toBuilder().content("").deletedAt(now).build()));
            case MESSAGE -> messageRepository.findById(report.getTargetId()).ifPresent(m -> messageRepository.save(m.toBuilder().deletedAt(now).build()));
        }
    }

    private boolean targetDeleted(Report r) {
        return switch (r.getTargetType()) {
            case CARD -> cardRepository.findById(r.getTargetId()).map(c -> c.getDeletedAt() != null).orElse(true);
            case COMMENT -> commentRepository.findById(r.getTargetId()).map(c -> c.getDeletedAt() != null).orElse(true);
            case MESSAGE -> messageRepository.findById(r.getTargetId()).map(m -> m.getDeletedAt() != null).orElse(true);
        };
    }

    private Report require(Long reportId) {
        return reportRepository.findById(reportId).orElseThrow(() -> new BusinessException(ReportErrorCode.REPORT_NOT_FOUND));
    }

    private Report unprocessed(Long reportId) {
        Report report = require(reportId);
        if (report.getStatus() == ReportStatus.RESOLVED || report.getStatus() == ReportStatus.REJECTED) {
            throw new BusinessException(AdminErrorCode.ADMIN_REPORT_ALREADY_PROCESSED);
        }
        return report;
    }

    private AdminReportResponse.Person person(Long accountId) {
        return accountId == null ? null : new AdminReportResponse.Person(accountId, accountRepository.findById(accountId).map(Account::getName).orElse(null));
    }

    private AdminReportResponse toResponse(Report r) {
        boolean open = r.getStatus() == ReportStatus.RECEIVED || r.getStatus() == ReportStatus.IN_PROGRESS;
        AdminReportResponse.RoomRef room = r.getRoomId() == null ? null : roomRepository.findById(r.getRoomId()).map(found -> new AdminReportResponse.RoomRef(found.getId(), found.getName())).orElse(null);
        return new AdminReportResponse(r.getId(), r.getTargetType(), r.getReason(), r.getStatus(), person(r.getReporterAccountId()), person(r.getTargetAccountId()), room, r.getCreatedAt(), open && r.getCreatedAt().isBefore(KstTime.nowUtc().minusDays(OVERDUE_DAYS)));
    }

    private AdminReportDetailResponse detail(Report r) {
        return new AdminReportDetailResponse(toResponse(r), r.getDetail(), r.getSnapshotJson(), targetDeleted(r), r.getAction(), r.getActionReason(), person(r.getHandledByAccountId()), r.getHandledAt());
    }
}
