package com.fiveoutoffive.moa_backend.integration.application.service;

import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.integration.domain.model.SlackDeliveryLog;
import com.fiveoutoffive.moa_backend.integration.domain.model.SlackDeliveryStatus;
import com.fiveoutoffive.moa_backend.integration.domain.model.SlackEventType;
import com.fiveoutoffive.moa_backend.integration.domain.model.SlackIntegration;
import com.fiveoutoffive.moa_backend.integration.domain.repository.SlackDeliveryLogRepository;
import com.fiveoutoffive.moa_backend.integration.domain.repository.SlackIntegrationRepository;
import com.fiveoutoffive.moa_backend.integration.infrastructure.crypto.IntegrationCipher;
import com.fiveoutoffive.moa_backend.session.domain.event.SessionClosedEvent;
import com.fiveoutoffive.moa_backend.task.domain.event.TaskDueSoonEvent;
import com.fiveoutoffive.moa_backend.vote.domain.event.VoteLifecycleEvent;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 도메인 이벤트를 커밋 후 비동기로 슬랙에 보낸다. 재시도 후에도 실패하면 이력을 남기고, 연속 실패가 기준을 넘으면 자동 비활성화한다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class SlackEventHandlers {

    private static final int MAX_RETRY = 1;

    private final SlackIntegrationRepository integrationRepository;
    private final SlackDeliveryLogRepository logRepository;
    private final SlackSender sender;
    private final IntegrationCipher cipher;

    @Value("${app.integration.slack-fail-threshold}")
    private int failThreshold;

    @Async
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    @org.springframework.transaction.event.TransactionalEventListener
    public void onVote(VoteLifecycleEvent event) {
        deliver(event.roomId(), event.closed() ? SlackEventType.VOTE_CLOSED : SlackEventType.VOTE_CREATED, (event.closed() ? "투표가 마감되었습니다: " : "새 투표가 등록되었습니다: ") + event.title());
    }

    @Async
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    @org.springframework.transaction.event.TransactionalEventListener
    public void onTaskDueSoon(TaskDueSoonEvent event) {
        deliver(event.roomId(), SlackEventType.TASK_DUE_SOON, "업무 마감이 임박했습니다: " + event.title());
    }

    @Async
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    @org.springframework.transaction.event.TransactionalEventListener
    public void onSessionClosed(SessionClosedEvent event) {
        deliver(event.roomId(), SlackEventType.SESSION_SUMMARY, "회차가 마감되었습니다: " + event.title());
    }

    private void deliver(Long roomId, SlackEventType type, String text) {
        Optional<SlackIntegration> found = integrationRepository.findByRoomId(roomId).filter(i -> Boolean.TRUE.equals(i.getEnabled()) && Arrays.asList(i.getEventTypes()).contains(type.name()));
        if (found.isEmpty()) {
            return;
        }
        SlackIntegration integration = found.get();
        String url = cipher.decrypt(integration.getWebhookUrlEnc());
        int attempts = 0;
        boolean ok = sender.send(url, text);
        while (!ok && attempts < MAX_RETRY) {
            attempts++;
            ok = sender.send(url, text);
        }
        logRepository.save(SlackDeliveryLog.builder().integrationId(integration.getId()).eventType(type).status(ok ? SlackDeliveryStatus.SUCCESS : SlackDeliveryStatus.FAILED).retryCount(attempts).errorMessage(ok ? null : "webhook request failed").build());
        int fails = ok ? 0 : integration.getConsecutiveFailCount() + 1;
        boolean disable = fails >= failThreshold;
        integrationRepository.save(integration.toBuilder().consecutiveFailCount(fails).enabled(!disable && integration.getEnabled()).disabledAt(disable ? KstTime.nowUtc() : integration.getDisabledAt()).build());
    }
}
