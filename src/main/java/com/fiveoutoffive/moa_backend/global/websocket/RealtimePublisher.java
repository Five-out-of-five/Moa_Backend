package com.fiveoutoffive.moa_backend.global.websocket;

import com.fiveoutoffive.moa_backend.global.util.KstTime;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
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
    private final MessageChannel brokerChannel;

    public RealtimePublisher(SimpMessagingTemplate messagingTemplate,
                             org.springframework.messaging.simp.user.SimpUserRegistry simpUserRegistry,
                             @org.springframework.beans.factory.annotation.Qualifier("clientOutboundChannel") MessageChannel clientOutboundChannel,
                             @org.springframework.beans.factory.annotation.Qualifier("brokerChannel") MessageChannel brokerChannel) {
        this.messagingTemplate = messagingTemplate;
        this.simpUserRegistry = simpUserRegistry;
        this.clientOutboundChannel = clientOutboundChannel;
        this.brokerChannel = brokerChannel;
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

    /**
     * 커밋 후, 사용자의 세션들에서 destination(과 그 하위 경로) 구독만 서버가 해제한다. 연결은 끊지 않는다.
     * 채널에서 내보내거나 나갔을 때 이후 메시지를 받지 않게 한다. (API 명세 '[STOMP] 채널 메시지 구독')
     */
    public void unsubscribe(Long accountId, String destination) {
        afterCommit(() -> {
            var user = simpUserRegistry.getUser(String.valueOf(accountId));
            if (user == null) {
                return;
            }
            user.getSessions().forEach(session -> session.getSubscriptions().stream()
                    .filter(sub -> sub.getDestination().equals(destination) || sub.getDestination().startsWith(destination + "/"))
                    .forEach(sub -> {
                        org.springframework.messaging.simp.SimpMessageHeaderAccessor accessor =
                                org.springframework.messaging.simp.SimpMessageHeaderAccessor.create(SimpMessageType.UNSUBSCRIBE);
                        accessor.setSessionId(session.getId());
                        accessor.setSubscriptionId(sub.getId());
                        accessor.setLeaveMutable(true);
                        brokerChannel.send(org.springframework.messaging.support.MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders()));
                    }));
        });
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
