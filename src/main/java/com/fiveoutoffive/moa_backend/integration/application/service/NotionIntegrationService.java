package com.fiveoutoffive.moa_backend.integration.application.service;

import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.global.util.Pages;
import com.fiveoutoffive.moa_backend.global.util.RedisKeys;
import com.fiveoutoffive.moa_backend.ideacard.domain.model.IdeaCard;
import com.fiveoutoffive.moa_backend.ideacard.domain.repository.IdeaCardRepository;
import com.fiveoutoffive.moa_backend.integration.application.command.NotionExportCommand;
import com.fiveoutoffive.moa_backend.integration.application.command.NotionIntegrationUpdateCommand;
import com.fiveoutoffive.moa_backend.integration.application.result.NotionAuthorizeResponse;
import com.fiveoutoffive.moa_backend.integration.application.result.NotionDatabaseResponse;
import com.fiveoutoffive.moa_backend.integration.application.result.NotionExportResponse;
import com.fiveoutoffive.moa_backend.integration.application.result.NotionIntegrationResponse;
import com.fiveoutoffive.moa_backend.integration.application.usecase.NotionIntegrationUseCase;
import com.fiveoutoffive.moa_backend.integration.domain.model.NotionExportLog;
import com.fiveoutoffive.moa_backend.integration.domain.model.NotionExportMode;
import com.fiveoutoffive.moa_backend.integration.domain.model.NotionExportStatus;
import com.fiveoutoffive.moa_backend.integration.domain.model.NotionIntegration;
import com.fiveoutoffive.moa_backend.integration.domain.repository.NotionExportLogRepository;
import com.fiveoutoffive.moa_backend.integration.domain.repository.NotionIntegrationRepository;
import com.fiveoutoffive.moa_backend.integration.exception.IntegrationErrorCode;
import com.fiveoutoffive.moa_backend.integration.infrastructure.crypto.IntegrationCipher;
import com.fiveoutoffive.moa_backend.room.application.service.RoomAccessService;
import com.fiveoutoffive.moa_backend.room.exception.RoomErrorCode;
import com.fiveoutoffive.moa_backend.session.domain.event.SessionClosedEvent;
import com.fiveoutoffive.moa_backend.session.domain.model.BoardSession;
import com.fiveoutoffive.moa_backend.session.domain.model.SessionStatus;
import com.fiveoutoffive.moa_backend.session.domain.repository.BoardSessionRepository;
import com.fiveoutoffive.moa_backend.session.exception.SessionErrorCode;
import com.fiveoutoffive.moa_backend.task.domain.repository.TaskRepository;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/** 노션 단방향 내보내기. 같은 회차를 다시 내보내면 기존 페이지를 갱신한다. */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NotionIntegrationService implements NotionIntegrationUseCase {

    private static final Duration STATE_TTL = Duration.ofMinutes(10);
    private static final String STATE_SEPARATOR = ":";

    private final NotionIntegrationRepository repository;
    private final NotionExportLogRepository logRepository;
    private final NotionClient notionClient;
    private final NotionFailureWriter failureWriter;
    private final IntegrationCipher cipher;
    private final RoomAccessService access;
    private final BoardSessionRepository sessionRepository;
    private final IdeaCardRepository cardRepository;
    private final TaskRepository taskRepository;
    private final StringRedisTemplate redisTemplate;

    @Value("${app.integration.notion-client-id}")
    private String clientId;
    @Value("${app.integration.notion-client-secret}")
    private String clientSecret;
    @Value("${app.integration.notion-redirect-uri}")
    private String redirectUri;
    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Transactional(readOnly = true)
    @Override
    public NotionIntegrationResponse get(Long accountId, Long roomId) {
        access.requireOwner(roomId, accountId);
        return repository.findByRoomId(roomId).map(this::toResponse).orElse(new NotionIntegrationResponse(false, null, null, null, null, false));
    }

    @Override
    public NotionAuthorizeResponse authorize(Long accountId, Long roomId) {
        access.requireOwner(roomId, accountId);
        String state = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(RedisKeys.NOTION_STATE_PREFIX + state, roomId + STATE_SEPARATOR + accountId, STATE_TTL);
        return new NotionAuthorizeResponse("https://api.notion.com/v1/oauth/authorize?owner=user&response_type=code&client_id=" + clientId
                + "&redirect_uri=" + java.net.URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) + "&state=" + state);
    }

    /** @return 프론트로 돌려보낼 주소 */
    @Override
    public String callback(String code, String state) {
        String value = redisTemplate.opsForValue().getAndDelete(RedisKeys.NOTION_STATE_PREFIX + state);
        if (value == null) {
            throw new BusinessException(IntegrationErrorCode.INTEGRATION_NOTION_STATE_INVALID);
        }
        String[] parts = value.split(STATE_SEPARATOR);
        Long roomId = Long.valueOf(parts[0]);
        Long accountId = Long.valueOf(parts[1]);
        // 프론트 워크스페이스 관리 화면. 연동 설정은 그 안의 연동 탭이다.
        String room = frontendUrl + "/workspaces/manage?room=" + roomId;
        String integrations = room + "&tab=integrations";
        if (code == null || code.isBlank()) {
            // 노션 연결 화면에서 취소했다(error=access_denied, code 없음). 연결하지 않고 연동 탭으로 돌려보낸다.
            return integrations + "&notion=cancelled";
        }
        if (!access.isOwner(roomId, accountId)) {
            // 연결을 시작한 뒤 방장이 아니게 됐다. 연동 탭은 방장 전용이라 같은 방의 기본 탭으로 보낸다.
            return room + "&notion=error&code=" + RoomErrorCode.ROOM_OWNER_ONLY.getCode();
        }
        try {
            var token = notionClient.exchangeCode(code, clientId, clientSecret, redirectUri);
            NotionIntegration.NotionIntegrationBuilder builder = repository.findByRoomId(roomId).map(NotionIntegration::toBuilder).orElseGet(() -> NotionIntegration.builder().roomId(roomId).createdByAccountId(accountId).exportMode(NotionExportMode.MANUAL));
            repository.save(builder.accessTokenEnc(cipher.encrypt(token.path("access_token").asString(""))).notionWorkspaceId(token.path("workspace_id").asString(null)).notionWorkspaceName(token.path("workspace_name").asString(null)).enabled(true).tokenExpiredAt(null).build());
            return integrations + "&notion=connected";
        } catch (Exception e) {
            log.warn("[Notion] 연결 실패: {}", e.getMessage());
            return integrations + "&notion=error&code=" + IntegrationErrorCode.INTEGRATION_NOTION_AUTH_EXPIRED.getCode();
        }
    }

    @Override
    public List<NotionDatabaseResponse> getDatabases(Long accountId, Long roomId) {
        access.requireOwner(roomId, accountId);
        NotionIntegration integration = require(roomId);
        try {
            return notionClient.searchDatabases(cipher.decrypt(integration.getAccessTokenEnc()));
        } catch (org.springframework.web.client.RestClientResponseException e) {
            throw expired(integration);
        }
    }

    @Override
    public NotionIntegrationResponse update(Long accountId, Long roomId, NotionIntegrationUpdateCommand command) {
        access.requireOwner(roomId, accountId);
        NotionIntegration integration = require(roomId);
        NotionDatabaseResponse target = findTargetDatabase(integration, command.targetDatabaseId());
        if (!repository.updateExportSettings(integration.getId(), target.id(), target.title(), command.exportMode(), KstTime.nowUtc())) {
            throw new BusinessException(IntegrationErrorCode.INTEGRATION_NOT_FOUND);
        }
        return toResponse(require(roomId));
    }

    @Override
    public NotionExportResponse export(Long accountId, Long roomId, NotionExportCommand command) {
        access.requireOwner(roomId, accountId);
        NotionIntegration integration = require(roomId);
        BoardSession session = sessionRepository.findById(command.sessionId()).filter(s -> s.getRoomId().equals(roomId) && s.getStatus() == SessionStatus.CLOSED).orElseThrow(() -> new BusinessException(SessionErrorCode.SESSION_NOT_FOUND));
        return doExport(integration, session);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<NotionExportResponse> getExports(Long accountId, Long roomId, Pageable pageable) {
        access.requireOwner(roomId, accountId);
        // API 명세에 이 API 의 에러가 없다. 연결 전이면 이력이 없는 것이므로 빈 페이지를 준다.
        return repository.findByRoomId(roomId)
                .map(integration -> Pages.response(logRepository.findByIntegrationId(integration.getId(), pageable).map(this::toResponse)))
                .orElseGet(() -> Pages.response(Page.<NotionExportResponse>empty(pageable)));
    }

    @Override
    public void delete(Long accountId, Long roomId) {
        access.requireOwner(roomId, accountId);
        repository.deleteById(require(roomId).getId());
    }

    @Async
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    @org.springframework.transaction.event.TransactionalEventListener
    @Override
    public void onSessionClosed(SessionClosedEvent event) {
        repository.findByRoomId(event.roomId()).filter(i -> i.getExportMode() == NotionExportMode.ON_SESSION_CLOSE && i.getTargetDatabaseId() != null).ifPresent(integration ->
                sessionRepository.findById(event.sessionId()).ifPresent(session -> {
                    try {
                        doExport(integration, session);
                    } catch (Exception e) {
                        log.warn("[Notion] 자동 내보내기 실패 sessionId={}: {}", event.sessionId(), e.getMessage());
                    }
                }));
    }

    private NotionExportResponse doExport(NotionIntegration integration, BoardSession session) {
        if (integration.getTargetDatabaseId() == null) {
            throw new BusinessException(IntegrationErrorCode.INTEGRATION_NOTION_DATABASE_NOT_FOUND);
        }
        // 내보내기 연타나 회차 마감 자동 내보내기와 겹쳐도 차례로 처리한다. 늦은 쪽은 아래에서 앞선 기록을 보고 새 페이지 대신 기존 페이지를 갱신한다.
        repository.lockExport(integration.getId());
        String token = cipher.decrypt(integration.getAccessTokenEnc());
        String content = exportContent(session);
        Optional<NotionExportLog> previous = logRepository.findByIntegrationIdAndSessionId(integration.getId(), session.getId());
        NotionExportLog.NotionExportLogBuilder builder = previous.map(NotionExportLog::toBuilder).orElseGet(() -> NotionExportLog.builder().integrationId(integration.getId()).sessionId(session.getId()));
        try {
            String pageId;
            if (previous.filter(p -> p.getNotionPageId() != null).isPresent()) {
                pageId = previous.get().getNotionPageId();
                notionClient.updatePage(token, pageId, content);
            } else {
                pageId = notionClient.createPage(token, integration.getTargetDatabaseId(), session.getTitle(), content);
            }
            return toResponse(logRepository.save(builder.notionPageId(pageId).status(NotionExportStatus.SUCCESS).errorMessage(null).exportedAt(KstTime.nowUtc()).build()));
        } catch (org.springframework.web.client.RestClientResponseException e) {
            // 바로 뒤에 예외를 던져 이 트랜잭션은 롤백되므로, 실패 이력은 따로 저장한다.
            failureWriter.saveLog(builder.status(NotionExportStatus.FAILED).errorMessage(String.valueOf(e.getStatusCode().value())).exportedAt(KstTime.nowUtc()).build());
            if (e.getStatusCode().value() == 401) {
                throw expired(integration);
            }
            throw new BusinessException(IntegrationErrorCode.INTEGRATION_NOTION_DATABASE_NOT_FOUND);
        }
    }

    /** 대상 데이터베이스가 실제로 있는지 확인하고 이름을 가져온다. */
    private NotionDatabaseResponse findTargetDatabase(NotionIntegration integration, String databaseId) {
        try {
            return notionClient.retrieveDatabase(cipher.decrypt(integration.getAccessTokenEnc()), databaseId);
        } catch (RestClientResponseException e) {
            switch (e.getStatusCode().value()) {
                case 401 -> throw expired(integration);
                // 400 은 ID 형식 오류, 403·404 는 없거나 연동에 공유되지 않은 데이터베이스
                case 400, 403, 404 -> throw new BusinessException(IntegrationErrorCode.INTEGRATION_NOTION_DATABASE_NOT_FOUND);
                default -> log.warn("[Notion] 데이터베이스 이름 조회 실패: {}", e.getStatusCode());
            }
        } catch (RestClientException e) {
            log.warn("[Notion] 데이터베이스 이름 조회 실패: {}", e.getMessage());
        }
        // 노션의 일시 장애(429·5xx·연결 실패)로 설정 저장까지 막지는 않는다. 대상이 그대로면 알고 있던 이름을 유지한다.
        return new NotionDatabaseResponse(databaseId, databaseId.equals(integration.getTargetDatabaseId()) ? integration.getTargetDatabaseTitle() : null);
    }

    private String exportContent(BoardSession session) {
        List<IdeaCard> cards = cardRepository.findBySessionIdAndDeletedAtIsNullOrderBySortOrderAsc(session.getId());
        Set<Long> cardIds = cards.stream().map(IdeaCard::getId).collect(Collectors.toSet());
        String cardText = cards.stream().map(c -> "[" + c.getCardType() + "] " + c.getTitle()).collect(Collectors.joining("\n"));
        String taskText = taskRepository.findBySourceCardIdInAndDeletedAtIsNull(cardIds).stream().map(t -> "- " + t.getTitle() + " (" + t.getStatus() + ")").collect(Collectors.joining("\n"));
        return "회고 카드\n" + cardText + "\n\n실행 과제\n" + taskText;
    }

    private BusinessException expired(NotionIntegration integration) {
        // 호출한 쪽이 이 예외로 롤백되므로 만료 표시는 따로 저장한다. 그래야 연동 조회에서 tokenExpired=true 가 보인다.
        failureWriter.markTokenExpired(integration);
        return new BusinessException(IntegrationErrorCode.INTEGRATION_NOTION_AUTH_EXPIRED);
    }

    private NotionIntegration require(Long roomId) {
        return repository.findByRoomId(roomId).orElseThrow(() -> new BusinessException(IntegrationErrorCode.INTEGRATION_NOT_FOUND));
    }

    private NotionIntegrationResponse toResponse(NotionIntegration i) {
        return new NotionIntegrationResponse(true, i.getNotionWorkspaceName(), i.getTargetDatabaseId() == null ? null : new NotionIntegrationResponse.TargetDatabase(i.getTargetDatabaseId(), i.getTargetDatabaseTitle()), i.getExportMode(), i.getEnabled(), i.getTokenExpiredAt() != null);
    }

    private NotionExportResponse toResponse(NotionExportLog l) {
        return new NotionExportResponse(l.getSessionId(), l.getNotionPageId(), l.getStatus(), l.getExportedAt());
    }
}
