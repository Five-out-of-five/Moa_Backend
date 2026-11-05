package com.fiveoutoffive.moa_backend.ai.domain.repository;

import com.fiveoutoffive.moa_backend.ai.domain.model.AiAssistantMessage;
import java.util.Optional;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface AiAssistantMessageRepository {

    AiAssistantMessage save(AiAssistantMessage aiAssistantMessage);

    Optional<AiAssistantMessage> findById(Long id);
}
