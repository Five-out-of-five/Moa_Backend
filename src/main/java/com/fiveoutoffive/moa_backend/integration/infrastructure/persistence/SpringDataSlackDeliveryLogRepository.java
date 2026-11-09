package com.fiveoutoffive.moa_backend.integration.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.integration.domain.model.SlackDeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSlackDeliveryLogRepository extends JpaRepository<SlackDeliveryLogJpaEntity, Long> {

    Page<SlackDeliveryLogJpaEntity> findByIntegrationId(Long integrationId, Pageable pageable);

    Page<SlackDeliveryLogJpaEntity> findByIntegrationIdAndStatus(Long integrationId, SlackDeliveryStatus status, Pageable pageable);
}
