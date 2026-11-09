package com.fiveoutoffive.moa_backend.report.application.service;

import com.fiveoutoffive.moa_backend.account.domain.model.Account;
import com.fiveoutoffive.moa_backend.account.domain.repository.AccountRepository;
import com.fiveoutoffive.moa_backend.account.exception.AccountErrorCode;
import com.fiveoutoffive.moa_backend.chat.domain.repository.ChatChannelRepository;
import com.fiveoutoffive.moa_backend.chat.domain.repository.ChatMessageRepository;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.ideacard.domain.repository.CardCommentRepository;
import com.fiveoutoffive.moa_backend.ideacard.domain.repository.IdeaCardRepository;
import com.fiveoutoffive.moa_backend.notification.application.service.NotificationSender;
import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationType;
import com.fiveoutoffive.moa_backend.report.application.command.ReportCreateCommand;
import com.fiveoutoffive.moa_backend.report.application.result.ReportResponse;
import com.fiveoutoffive.moa_backend.report.application.usecase.ReportUseCase;
import com.fiveoutoffive.moa_backend.report.domain.model.Report;
import com.fiveoutoffive.moa_backend.report.domain.model.ReportStatus;
import com.fiveoutoffive.moa_backend.report.domain.model.ReportTargetType;
import com.fiveoutoffive.moa_backend.report.domain.repository.ReportRepository;
import com.fiveoutoffive.moa_backend.report.exception.ReportErrorCode;
import com.fiveoutoffive.moa_backend.room.application.service.RoomAccessService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 신고 접수. 신고 시점의 내용을 스냅샷으로 저장해 이후 수정·삭제돼도 확인할 수 있게 한다. */
@Service
@Transactional
@RequiredArgsConstructor
public class ReportService implements ReportUseCase {

    private final ReportRepository reportRepository;
    private final AccountRepository accountRepository;
    private final IdeaCardRepository cardRepository;
    private final CardCommentRepository commentRepository;
    private final ChatMessageRepository messageRepository;
    private final ChatChannelRepository channelRepository;
    private final RoomAccessService access;
    private final NotificationSender notificationSender;
    private final tools.jackson.databind.json.JsonMapper jsonMapper;

    @Override
    public ReportResponse create(Long accountId, ReportCreateCommand command) {
        Account reporter = accountRepository.findById(accountId).orElseThrow(() -> new BusinessException(AccountErrorCode.ACCOUNT_NOT_FOUND));
        if (reporter.getReportBlockedUntil() != null && reporter.getReportBlockedUntil().isAfter(KstTime.nowUtc())) {
            throw new BusinessException(ReportErrorCode.REPORT_BLOCKED);
        }
        Target target = resolve(command.targetType(), command.targetId());
        access.requireMember(target.roomId(), accountId);
        if (accountId.equals(target.writerId())) {
            throw new BusinessException(ReportErrorCode.REPORT_OWN_CONTENT);
        }
        if (reportRepository.existsByReporterAccountIdAndTargetTypeAndTargetId(accountId, command.targetType(), command.targetId())) {
            throw new BusinessException(ReportErrorCode.REPORT_DUPLICATED);
        }
        Report saved = reportRepository.save(Report.builder().reporterAccountId(accountId).roomId(target.roomId()).targetType(command.targetType()).targetId(command.targetId()).targetAccountId(target.writerId())
                .reason(command.reason()).detail(command.detail()).snapshotJson(jsonMapper.writeValueAsString(Map.of("content", target.content()))).status(ReportStatus.RECEIVED).isFalseReport(false).build());
        notificationSender.send(null, null, NotificationType.REPORT_RECEIVED, List.of(accountId), "신고가 접수되었습니다", null, "REPORT", saved.getId());
        return new ReportResponse(saved.getId(), saved.getStatus(), saved.getCreatedAt());
    }

    private Target resolve(ReportTargetType type, Long id) {
        return switch (type) {
            case CARD -> cardRepository.findById(id).filter(c -> c.getDeletedAt() == null)
                    .map(c -> new Target(c.getRoomId(), c.getWriterAccountId(), c.getTitle() + "\n" + c.getContent())).orElseThrow(this::notFound);
            case COMMENT -> commentRepository.findById(id).filter(c -> c.getDeletedAt() == null).flatMap(c -> cardRepository.findById(c.getCardId())
                    .map(card -> new Target(card.getRoomId(), c.getWriterAccountId(), c.getContent()))).orElseThrow(this::notFound);
            case MESSAGE -> messageRepository.findById(id).filter(m -> m.getDeletedAt() == null).flatMap(m -> channelRepository.findById(m.getChannelId())
                    .map(ch -> new Target(ch.getRoomId(), m.getSenderAccountId(), String.valueOf(m.getContent())))).orElseThrow(this::notFound);
        };
    }

    private BusinessException notFound() {
        return new BusinessException(ReportErrorCode.REPORT_TARGET_NOT_FOUND);
    }

    private record Target(Long roomId, Long writerId, String content) {
    }
}
