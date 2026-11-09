package com.fiveoutoffive.moa_backend.briefing.application.service;

import com.fiveoutoffive.moa_backend.briefing.domain.model.BoardEvent;
import com.fiveoutoffive.moa_backend.briefing.domain.model.BoardEventTargetType;
import com.fiveoutoffive.moa_backend.briefing.domain.model.BoardEventType;
import com.fiveoutoffive.moa_backend.briefing.domain.repository.BoardEventRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 방 변경 이벤트 기록의 단일 창구. 캐치업 브리핑의 원천이다. */
@Component
@RequiredArgsConstructor
public class BoardEventRecorder {

    private final BoardEventRepository repository;
    private final tools.jackson.databind.json.JsonMapper jsonMapper;

    /** @param title 브리핑에 보일 대상 제목. 없으면 null */
    public void record(Long roomId, BoardEventType type, Long actorId, Long recipientId, BoardEventTargetType targetType, Long targetId, String title) {
        String payload = title == null ? null : jsonMapper.writeValueAsString(Map.of("title", title));
        repository.save(BoardEvent.builder().roomId(roomId).eventType(type).actorAccountId(actorId).recipientAccountId(recipientId)
                .targetType(targetType).targetId(targetId).payload(payload).build());
    }

    public String titleOf(BoardEvent event) {
        if (event.getPayload() == null) {
            return null;
        }
        return jsonMapper.readTree(event.getPayload()).path("title").asString(null);
    }
}
