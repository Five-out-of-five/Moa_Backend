package com.fiveoutoffive.moa_backend.ai.domain.repository;

import com.fiveoutoffive.moa_backend.ai.domain.model.AiSummary;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface AiSummaryRepository {

    AiSummary save(AiSummary aiSummary);

    Optional<AiSummary> findById(Long id);

    Page<AiSummary> findByRoomId(Long roomId, Pageable pageable);
}
