package com.fiveoutoffive.moa_backend.global.websocket;

import com.fiveoutoffive.moa_backend.global.util.KstTime;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 변경 API 가 저장 후 보내는 실시간 이벤트의 단일 창구. (API 명세 공통 규칙 "실시간")
 *
 * <p>트랜잭션 안에서 호출하면 커밋 후에 보낸다. 롤백된 변경이 다른 멤버 화면에 나타나지 않게 하기 위해서다.
 */
@Component
public class RealtimePublisher {

    private final SimpMessagingTemplate messagingTemplate;
    private final org.springframework.messaging.simp.user.SimpUserRegistry simpUserRegistry;
    private final MessageChannel clientOutboundChannel;

    public RealtimePublisher(SimpMessagingTemplate messagingTemplate,
                             org.springframework.messaging.simp.user.SimpUserRegistry simpUserRegistry,
                             @org.springframework.beans.factory.annotation.Qualifier("clientOutboundChannel") MessageChannel clientOutboundChannel) {
        this.messagingTemplate = messagingTemplate;
        this.simpUserRegistry = simpUserRegistry;
        this.clientOutboundChannel = clientOutboundChannel;
    }

    /** 해당 사용자의 모든 STOMP 세션을 끊는다. */
    public void disconnectUser(String accountId) {
        var user = simpUserRegistry.getUser(accountId);
        if (user == null) {
            return;
        }
        user.getSessions().forEach(session ->
                clientOutboundChannel.send(RealtimeDisconnectSupport.disconnectMessage(session.getId())));
    }

    /** /topic/rooms/{roomId} */
    public void toRoom(Long roomId, String type, Object payload, Long actorAccountId) {
        RoomEvent event = new RoomEvent(type, payload, KstTime.nowUtc(), actorAccountId);
        afterCommit(() -> messagingTemplate.convertAndSend("/topic/rooms/" + roomId, event));
    }

    /** /topic/channels/{channelId} */
    public void toChannel(Long channelId, String type, Object payload) {
        ChannelEvent event = new ChannelEvent(type, payload);
        afterCommit(() -> messagingTemplate.convertAndSend("/topic/channels/" + channelId, event));
    }

    /** /user/queue/{queue} */
    public void toUser(Long accountId, String queue, Object payload) {
        afterCommit(() -> messagingTemplate.convertAndSendToUser(String.valueOf(accountId), "/queue/" + queue, payload));
    }

    private void afterCommit(Runnable send) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            send.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                send.run();
            }
        });
    }

    public record RoomEvent(String type, Object payload, LocalDateTime occurredAt, Long actorAccountId) {
    }

    public record ChannelEvent(String type, Object payload) {
    }
}
