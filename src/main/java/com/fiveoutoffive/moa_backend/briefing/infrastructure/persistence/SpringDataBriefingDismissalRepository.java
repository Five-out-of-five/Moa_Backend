package com.fiveoutoffive.moa_backend.briefing.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.briefing.domain.model.BoardEventType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataBriefingDismissalRepository extends JpaRepository<BriefingDismissalJpaEntity, Long> {

    List<BriefingDismissalJpaEntity> findByRoomIdAndAccountId(Long roomId, Long accountId);

    void deleteByRoomIdAndAccountId(Long roomId, Long accountId);

    Optional<BriefingDismissalJpaEntity> findByRoomIdAndAccountIdAndEventType(Long roomId, Long accountId, BoardEventType eventType);
}
