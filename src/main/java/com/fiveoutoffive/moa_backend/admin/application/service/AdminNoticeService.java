package com.fiveoutoffive.moa_backend.admin.application.service;

import com.fiveoutoffive.moa_backend.account.domain.model.Account;
import com.fiveoutoffive.moa_backend.account.domain.repository.AccountRepository;
import com.fiveoutoffive.moa_backend.admin.application.command.NoticeCommand;
import com.fiveoutoffive.moa_backend.admin.application.result.AdminNoticeResponse;
import com.fiveoutoffive.moa_backend.admin.application.usecase.AdminNoticeUseCase;
import com.fiveoutoffive.moa_backend.admin.exception.AdminErrorCode;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.global.util.Pages;
import com.fiveoutoffive.moa_backend.notice.application.result.NoticeResponse;
import com.fiveoutoffive.moa_backend.notice.application.service.NoticeBroadcaster;
import com.fiveoutoffive.moa_backend.notice.domain.model.Notice;
import com.fiveoutoffive.moa_backend.notice.domain.model.NoticeType;
import com.fiveoutoffive.moa_backend.notice.domain.repository.NoticeRepository;
import com.fiveoutoffive.moa_backend.notice.exception.NoticeErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminNoticeService implements AdminNoticeUseCase {

    private final NoticeRepository noticeRepository;
    private final AccountRepository accountRepository;
    private final NoticeBroadcaster broadcaster;
    private final AdminAuditRecorder audit;

    @Transactional(readOnly = true)
    @Override
    public PageResponse<AdminNoticeResponse> getNotices(NoticeType noticeType, String state, Pageable pageable) {
        LocalDateTime now = KstTime.nowUtc();
        List<AdminNoticeResponse> filtered = noticeRepository.findByDeletedAtIsNull(Pageable.unpaged(pageable.getSort())).stream()
                .filter(n -> noticeType == null || n.getNoticeType() == noticeType)
                .filter(n -> state == null || state.equals(stateOf(n, now)))
                .map(n -> new AdminNoticeResponse(n.getId(), n.getNoticeType(), n.getTitle(), n.getDisplayStartsAt(), n.getDisplayEndsAt(), stateOf(n, now), author(n.getAuthorAccountId())))
                .toList();
        int from = Math.min((int) pageable.getOffset(), filtered.size());
        int to = Math.min(from + pageable.getPageSize(), filtered.size());
        return Pages.response(new PageImpl<>(filtered.subList(from, to), pageable, filtered.size()));
    }

    @Override
    public NoticeResponse create(Long adminId, NoticeCommand command) {
        LocalDateTime start = KstTime.toUtc(command.displayStartsAt());
        LocalDateTime end = KstTime.toUtc(command.displayEndsAt());
        validate(command.noticeType(), start, end, null);
        Notice saved = noticeRepository.save(Notice.builder().noticeType(command.noticeType()).title(command.title()).content(command.content()).displayStartsAt(start).displayEndsAt(end).maintenanceStartsAt(KstTime.toUtc(command.maintenanceStartsAt())).authorAccountId(adminId).build());
        if (!start.isAfter(KstTime.nowUtc())) {
            broadcaster.broadcast(saved);
        }
        audit.record(adminId, "NOTICE_CREATE", "NOTICE", saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public NoticeResponse getNotice(Long noticeId) {
        return toResponse(require(noticeId));
    }

    @Override
    public NoticeResponse update(Long adminId, Long noticeId, NoticeCommand command) {
        Notice notice = require(noticeId);
        LocalDateTime start = KstTime.toUtc(command.displayStartsAt());
        LocalDateTime end = KstTime.toUtc(command.displayEndsAt());
        validate(command.noticeType(), start, end, noticeId);
        Notice saved = noticeRepository.save(notice.toBuilder().noticeType(command.noticeType()).title(command.title()).content(command.content()).displayStartsAt(start).displayEndsAt(end).maintenanceStartsAt(KstTime.toUtc(command.maintenanceStartsAt())).build());
        audit.record(adminId, "NOTICE_UPDATE", "NOTICE", noticeId, null);
        return toResponse(saved);
    }

    @Override
    public void delete(Long adminId, Long noticeId) {
        noticeRepository.save(require(noticeId).toBuilder().deletedAt(KstTime.nowUtc()).build());
        audit.record(adminId, "NOTICE_DELETE", "NOTICE", noticeId, null);
    }

    private void validate(NoticeType type, LocalDateTime start, LocalDateTime end, Long excludeId) {
        if (!end.isAfter(start)) {
            throw new BusinessException(AdminErrorCode.ADMIN_NOTICE_PERIOD_INVALID);
        }
        if (type == NoticeType.URGENT && noticeRepository.countOverlapping(NoticeType.URGENT, start, end, excludeId) > 0) {
            throw new BusinessException(AdminErrorCode.ADMIN_NOTICE_BANNER_CONFLICT);
        }
    }

    private Notice require(Long noticeId) {
        return noticeRepository.findById(noticeId).filter(n -> n.getDeletedAt() == null).orElseThrow(() -> new BusinessException(NoticeErrorCode.NOTICE_NOT_FOUND));
    }

    private String stateOf(Notice n, LocalDateTime now) {
        return n.getDisplayStartsAt().isAfter(now) ? "SCHEDULED" : n.getDisplayEndsAt().isAfter(now) ? "ACTIVE" : "ENDED";
    }

    private AdminNoticeResponse.Author author(Long id) {
        return new AdminNoticeResponse.Author(id, accountRepository.findById(id).map(Account::getName).orElse(null));
    }

    private NoticeResponse toResponse(Notice n) {
        return new NoticeResponse(n.getId(), n.getNoticeType(), n.getTitle(), n.getContent(), n.getDisplayStartsAt(), n.getDisplayEndsAt(), n.getMaintenanceStartsAt());
    }
}
