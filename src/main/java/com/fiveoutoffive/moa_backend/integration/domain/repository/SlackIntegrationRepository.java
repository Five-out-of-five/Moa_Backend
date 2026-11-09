package com.fiveoutoffive.moa_backend.integration.domain.repository;

import com.fiveoutoffive.moa_backend.integration.domain.model.SlackIntegration;
import java.util.Optional;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface SlackIntegrationRepository {

    SlackIntegration save(SlackIntegration slackIntegration);

    Optional<SlackIntegration> findById(Long id);

    Optional<SlackIntegration> findByRoomId(Long roomId);

    void deleteById(Long id);
}
