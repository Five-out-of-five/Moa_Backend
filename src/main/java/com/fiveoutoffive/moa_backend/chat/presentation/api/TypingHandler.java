package com.fiveoutoffive.moa_backend.chat.presentation.api;

import com.fiveoutoffive.moa_backend.chat.application.usecase.ChatUseCase;
import com.fiveoutoffive.moa_backend.global.common.api.response.MemberRef;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * 입력 중 표시. 저장하지 않고 채널 참여자에게 전달만 한다. (API 명세 '[STOMP] 입력 중 전송')
 *
 * <p>displayName 은 명세 공통 규칙(MemberRef)대로 채널이 속한 방의 닉네임, 없으면 기본 이름이다.
 * 채널 참여 여부는 SEND 프레임에서 {@code StompAuthChannelInterceptor} 가 먼저 확인한다.
 */
@Controller
@RequiredArgsConstructor
public class TypingHandler {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatUseCase chatUseCase;

    @MessageMapping("/channels/{channelId}/typing")
    public void typing(@DestinationVariable Long channelId, @Payload TypingMessage message, Principal principal) {
        Long accountId = Long.valueOf(principal.getName());
        // 채널이 삭제되었으면 보낼 대상이 없으므로 무시한다.
        chatUseCase.typingMember(accountId, channelId).ifPresent(member ->
                messagingTemplate.convertAndSend("/topic/channels/" + channelId + "/typing",
                        new TypingEvent(accountId, displayName(member), Boolean.TRUE.equals(message.typing()))));
    }

    private String displayName(MemberRef member) {
        return member == null ? null : member.displayName();
    }

    public record TypingMessage(Boolean typing) {
    }

    public record TypingEvent(Long accountId, String displayName, boolean typing) {
    }
}
