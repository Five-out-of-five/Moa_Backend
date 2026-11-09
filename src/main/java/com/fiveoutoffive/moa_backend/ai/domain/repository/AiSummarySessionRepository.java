package com.fiveoutoffive.moa_backend.ai.domain.repository;

import com.fiveoutoffive.moa_backend.ai.domain.model.AiSummarySession;
import java.util.List;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface AiSummarySessionRepository {

    AiSummarySession save(AiSummarySession aiSummarySession);

    List<AiSummarySession> findBySummaryId(Long summaryId);
}
