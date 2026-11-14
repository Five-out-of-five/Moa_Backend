package com.fiveoutoffive.moa_backend.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiveoutoffive.moa_backend.chat.application.usecase.ChatUseCase;
import com.fiveoutoffive.moa_backend.chat.presentation.api.TypingHandler;
import com.fiveoutoffive.moa_backend.chat.presentation.api.TypingHandler.TypingEvent;
import com.fiveoutoffive.moa_backend.global.common.api.response.MemberRef;
import com.fiveoutoffive.moa_backend.global.type.MemberStatus;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

class TypingHandlerTest {

    private final SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
    private final ChatUseCase chatUseCase = mock(ChatUseCase.class);
    private final TypingHandler handler = new TypingHandler(messagingTemplate, chatUseCase);

    @Test
    void 방별_표시_이름으로_입력_중을_전달한다() {
        MemberRef member = MemberRef.of(7L, "방닉네임", null, MemberStatus.ACTIVE);
        when(chatUseCase.typingMember(7L, 3L)).thenReturn(Optional.of(member));

        handler.typing(3L, new TypingHandler.TypingMessage(true), () -> "7");

        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(org.mockito.ArgumentMatchers.eq("/topic/channels/3/typing"), payload.capture());
        assertThat(payload.getValue()).isEqualTo(new TypingEvent(7L, "방닉네임", true));
    }

    @Test
    void 채널이_없으면_보내지_않는다() {
        when(chatUseCase.typingMember(7L, 3L)).thenReturn(Optional.empty());

        handler.typing(3L, new TypingHandler.TypingMessage(false), () -> "7");

        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }
}
