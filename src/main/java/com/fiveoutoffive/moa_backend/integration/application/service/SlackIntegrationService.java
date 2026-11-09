package com.fiveoutoffive.moa_backend.integration.application.service;

import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.util.Pages;
import com.fiveoutoffive.moa_backend.integration.application.command.SlackIntegrationCommand;
import com.fiveoutoffive.moa_backend.integration.application.command.SlackTestCommand;
import com.fiveoutoffive.moa_backend.integration.application.result.SlackDeliveryResponse;
import com.fiveoutoffive.moa_backend.integration.application.result.SlackIntegrationResponse;
import com.fiveoutoffive.moa_backend.integration.application.usecase.SlackIntegrationUseCase;
import com.fiveoutoffive.moa_backend.integration.domain.model.SlackDeliveryLog;
import com.fiveoutoffive.moa_backend.integration.domain.model.SlackDeliveryStatus;
import com.fiveoutoffive.moa_backend.integration.domain.model.SlackEventType;
import com.fiveoutoffive.moa_backend.integration.domain.model.SlackIntegration;
import com.fiveoutoffive.moa_backend.integration.domain.repository.SlackDeliveryLogRepository;
import com.fiveoutoffive.moa_backend.integration.domain.repository.SlackIntegrationRepository;
import com.fiveoutoffive.moa_backend.integration.exception.IntegrationErrorCode;
import com.fiveoutoffive.moa_backend.integration.infrastructure.crypto.IntegrationCipher;
import com.fiveoutoffive.moa_backend.room.application.service.RoomAccessService;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SlackIntegrationService implements SlackIntegrationUseCase {

    private static final String WEBHOOK_PREFIX = "https://hooks.slack.com/services/";

    private final SlackIntegrationRepository repository;
    private final SlackDeliveryLogRepository logRepository;
    private final SlackSender sender;
    private final IntegrationCipher cipher;
    private final RoomAccessService access;

    @Transactional(readOnly = true)
    @Override
    public SlackIntegrationResponse get(Long accountId, Long roomId) {
        access.requireOwner(roomId, accountId);
        return repository.findByRoomId(roomId).map(this::toResponse).orElse(new SlackIntegrationResponse(false, null, List.of(), null, null, null));
    }

    @Override
    public void test(Long accountId, Long roomId, SlackTestCommand command) {
        access.requireOwner(roomId, accountId);
        requireUrl(command.webhookUrl());
        if (!sender.send(command.webhookUrl(), "모아 연동 테스트 메시지입니다.")) {
            throw new BusinessException(IntegrationErrorCode.INTEGRATION_SLACK_TEST_FAILED);
        }
    }

    @Override
    public SlackIntegrationResponse upsert(Long accountId, Long roomId, SlackIntegrationCommand command) {
        access.requireOwner(roomId, accountId);
        Optional<SlackIntegration> existing = repository.findByRoomId(roomId);
        if (command.webhookUrl() != null) {
            requireUrl(command.webhookUrl());
        } else if (existing.isEmpty()) {
            throw new BusinessException(IntegrationErrorCode.INTEGRATION_SLACK_URL_INVALID);
        }
        byte[] encrypted = command.webhookUrl() != null ? cipher.encrypt(command.webhookUrl()) : existing.get().getWebhookUrlEnc();
        String[] events = command.eventTypes().stream().map(Enum::name).toArray(String[]::new);
        SlackIntegration.SlackIntegrationBuilder builder = existing.map(SlackIntegration::toBuilder).orElseGet(() -> SlackIntegration.builder().roomId(roomId).createdByAccountId(accountId));
        return toResponse(repository.save(builder.webhookUrlEnc(encrypted).eventTypes(events).enabled(true).consecutiveFailCount(0).disabledAt(null).build()));
    }

    @Override
    public void delete(Long accountId, Long roomId) {
        access.requireOwner(roomId, accountId);
        repository.deleteById(require(roomId).getId());
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<SlackDeliveryResponse> getDeliveries(Long accountId, Long roomId, SlackDeliveryStatus status, Pageable pageable) {
        access.requireOwner(roomId, accountId);
        Long id = require(roomId).getId();
        Page<SlackDeliveryLog> page = status == null ? logRepository.findByIntegrationId(id, pageable) : logRepository.findByIntegrationIdAndStatus(id, status, pageable);
        return Pages.response(page.map(l -> new SlackDeliveryResponse(l.getEventType(), l.getStatus(), l.getRetryCount(), l.getErrorMessage(), l.getCreatedAt())));
    }

    private SlackIntegration require(Long roomId) {
        return repository.findByRoomId(roomId).orElseThrow(() -> new BusinessException(IntegrationErrorCode.INTEGRATION_NOT_FOUND));
    }

    private void requireUrl(String url) {
        if (!url.startsWith(WEBHOOK_PREFIX)) {
            throw new BusinessException(IntegrationErrorCode.INTEGRATION_SLACK_URL_INVALID);
        }
    }

    private SlackIntegrationResponse toResponse(SlackIntegration i) {
        String url = cipher.decrypt(i.getWebhookUrlEnc());
        String masked = url.substring(0, Math.min(WEBHOOK_PREFIX.length() + 4, url.length())) + "****";
        return new SlackIntegrationResponse(true, masked, Arrays.stream(i.getEventTypes()).map(SlackEventType::valueOf).toList(), i.getEnabled(), i.getConsecutiveFailCount(), i.getDisabledAt());
    }
}
