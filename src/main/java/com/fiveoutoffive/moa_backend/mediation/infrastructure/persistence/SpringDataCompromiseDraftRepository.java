package com.fiveoutoffive.moa_backend.mediation.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.mediation.domain.model.DraftStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCompromiseDraftRepository extends JpaRepository<CompromiseDraftJpaEntity, Long> {

    List<CompromiseDraftJpaEntity> findByConflictIdAndRequestedByAccountIdAndStatus(Long conflictId, Long requestedByAccountId, DraftStatus status);
}
