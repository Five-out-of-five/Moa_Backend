package com.fiveoutoffive.moa_backend.integration.domain.repository;

import com.fiveoutoffive.moa_backend.integration.domain.model.SlackDeliveryLog;
import com.fiveoutoffive.moa_backend.integration.domain.model.SlackDeliveryStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface SlackDeliveryLogRepository {

    SlackDeliveryLog save(SlackDeliveryLog slackDeliveryLog);

    Optional<SlackDeliveryLog> findById(Long id);

    Page<SlackDeliveryLog> findByIntegrationId(Long integrationId, Pageable pageable);

    Page<SlackDeliveryLog> findByIntegrationIdAndStatus(Long integrationId, SlackDeliveryStatus status, Pageable pageable);
}
