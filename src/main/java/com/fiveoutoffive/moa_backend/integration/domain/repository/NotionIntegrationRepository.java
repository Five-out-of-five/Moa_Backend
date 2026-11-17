package com.fiveoutoffive.moa_backend.integration.domain.repository;

import com.fiveoutoffive.moa_backend.integration.domain.model.NotionIntegration;
import java.util.Optional;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface NotionIntegrationRepository {

    NotionIntegration save(NotionIntegration notionIntegration);

    Optional<NotionIntegration> findById(Long id);

    Optional<NotionIntegration> findByRoomId(Long roomId);

    void deleteById(Long id);

    /** 이 연동의 내보내기 잠금을 건다(트랜잭션 끝까지). 같은 연동의 내보내기는 차례로 처리된다. 트랜잭션 안에서만 쓴다. */
    void lockExport(Long integrationId);
}
