package com.fiveoutoffive.moa_backend.integration.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataNotionIntegrationRepository extends JpaRepository<NotionIntegrationJpaEntity, Long> {

    Optional<NotionIntegrationJpaEntity> findByRoomId(Long roomId);

    void deleteById(Long id);
}
