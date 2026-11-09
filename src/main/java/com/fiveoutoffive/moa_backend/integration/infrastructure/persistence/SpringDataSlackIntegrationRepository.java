package com.fiveoutoffive.moa_backend.integration.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSlackIntegrationRepository extends JpaRepository<SlackIntegrationJpaEntity, Long> {

    Optional<SlackIntegrationJpaEntity> findByRoomId(Long roomId);

    void deleteById(Long id);
}
