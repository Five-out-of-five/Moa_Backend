package com.fiveoutoffive.moa_backend.notice.application.service;

import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.global.util.Pages;
import com.fiveoutoffive.moa_backend.notice.application.result.ActiveNoticeResponse;
import com.fiveoutoffive.moa_backend.notice.application.result.NoticeResponse;
import com.fiveoutoffive.moa_backend.notice.application.result.NoticeSummaryResponse;
import com.fiveoutoffive.moa_backend.notice.application.usecase.NoticeUseCase;
import com.fiveoutoffive.moa_backend.notice.domain.model.Notice;
import com.fiveoutoffive.moa_backend.notice.domain.model.NoticeDismissal;
import com.fiveoutoffive.moa_backend.notice.domain.model.NoticeType;
import com.fiveoutoffive.moa_backend.notice.domain.repository.NoticeDismissalRepository;
import com.fiveoutoffive.moa_backend.notice.domain.repository.NoticeRepository;
import com.fiveoutoffive.moa_backend.notice.exception.NoticeErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NoticeService implements NoticeUseCase {

    private final NoticeRepository noticeRepository;
    private final NoticeDismissalRepository dismissalRepository;

    @Transactional(readOnly = true)
    @Override
    public ActiveNoticeResponse getActive(Long accountId) {
        LocalDateTime now = KstTime.nowUtc();
        Map<Long, LocalDateTime> dismissed = dismissalRepository.findByAccountId(accountId).stream().collect(Collectors.toMap(NoticeDismissal::getNoticeId, NoticeDismissal::getDismissedAt));
        // 점검 공지는 닫았어도 점검 시작 전에는 다시 보인다.
        List<Notice> active = noticeRepository.findActive(now).stream().filter(n -> !dismissed.containsKey(n.getId())
                || (n.getNoticeType() == NoticeType.MAINTENANCE && n.getMaintenanceStartsAt() != null && n.getMaintenanceStartsAt().isAfter(now))).toList();
        ActiveNoticeResponse.Banner banner = active.stream().filter(n -> n.getNoticeType() == NoticeType.URGENT).findFirst().map(n -> new ActiveNoticeResponse.Banner(n.getId(), n.getNoticeType(), n.getTitle())).orElse(null);
        return new ActiveNoticeResponse(banner, active.stream().map(n -> new ActiveNoticeResponse.Item(n.getId(), n.getNoticeType(), n.getTitle(), n.getDisplayEndsAt())).toList());
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<NoticeSummaryResponse> getList(Pageable pageable) {
        return Pages.response(noticeRepository.findByDeletedAtIsNull(pageable).map(n -> new NoticeSummaryResponse(n.getId(), n.getNoticeType(), n.getTitle(), n.getDisplayStartsAt())));
    }

    @Transactional(readOnly = true)
    @Override
    public NoticeResponse getDetail(Long noticeId) {
        Notice n = noticeRepository.findById(noticeId).filter(found -> found.getDeletedAt() == null).orElseThrow(() -> new BusinessException(NoticeErrorCode.NOTICE_NOT_FOUND));
        return new NoticeResponse(n.getId(), n.getNoticeType(), n.getTitle(), n.getContent(), n.getDisplayStartsAt(), n.getDisplayEndsAt(), n.getMaintenanceStartsAt());
    }

    @Override
    public void dismiss(Long accountId, Long noticeId) {
        getDetail(noticeId);
        dismissalRepository.save(NoticeDismissal.builder().noticeId(noticeId).accountId(accountId).dismissedAt(KstTime.nowUtc()).build());
    }
}
