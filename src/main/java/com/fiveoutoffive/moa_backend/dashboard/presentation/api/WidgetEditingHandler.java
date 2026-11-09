package com.fiveoutoffive.moa_backend.dashboard.presentation.api;

import com.fiveoutoffive.moa_backend.global.websocket.RealtimePublisher;
import java.security.Principal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

/** 다른 멤버가 옮기는 위젯에 '편집 중'을 표시한다. 저장하지 않고 전달만 한다. */
@Controller
@RequiredArgsConstructor
public class WidgetEditingHandler {

    private final RealtimePublisher realtime;

    @MessageMapping("/rooms/{roomId}/widgets/{widgetId}/editing")
    public void editing(@DestinationVariable Long roomId, @DestinationVariable Long widgetId, @Payload WidgetEditingMessage message, Principal principal) {
        Long accountId = Long.valueOf(principal.getName());
        realtime.toRoom(roomId, "WIDGET_EDITING", Map.of("widgetId", widgetId, "accountId", accountId, "editing", Boolean.TRUE.equals(message.editing())), accountId);
    }

    public record WidgetEditingMessage(Boolean editing) {
    }
}
