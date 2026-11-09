package com.fiveoutoffive.moa_backend.chat.presentation.api;

import com.fiveoutoffive.moa_backend.account.domain.model.Account;
import com.fiveoutoffive.moa_backend.account.domain.repository.AccountRepository;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/** 입력 중 표시. 저장하지 않고 채널 참여자에게 전달만 한다. */
@Controller
@RequiredArgsConstructor
public class TypingHandler {

    private final SimpMessagingTemplate messagingTemplate;
    private final AccountRepository accountRepository;

    @MessageMapping("/channels/{channelId}/typing")
    public void typing(@DestinationVariable Long channelId, @Payload TypingMessage message, Principal principal) {
        Long accountId = Long.valueOf(principal.getName());
        String name = accountRepository.findById(accountId).map(Account::getName).orElse(null);
        messagingTemplate.convertAndSend("/topic/channels/" + channelId + "/typing", new TypingEvent(accountId, name, Boolean.TRUE.equals(message.typing())));
    }

    public record TypingMessage(Boolean typing) {
    }

    public record TypingEvent(Long accountId, String displayName, boolean typing) {
    }
}
