package com.fiveoutoffive.moa_backend.briefing.infrastructure.persistence;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import com.fiveoutoffive.moa_backend.briefing.domain.model.BoardEventType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataBriefingDismissalRepository extends JpaRepository<BriefingDismissalJpaEntity, Long> {

    List<BriefingDismissalJpaEntity> findByRoomIdAndAccountId(Long roomId, Long accountId);

    void deleteByRoomIdAndAccountId(Long roomId, Long accountId);

    Optional<BriefingDismissalJpaEntity> findByRoomIdAndAccountIdAndEventType(Long roomId, Long accountId, BoardEventType eventType);

    /** 소식 지우기를 한 번에 넣거나 갱신한다. 같은 요청이 동시에 와도 유니크 위반이 나지 않는다. */
    @Modifying(flushAutomatically = true)
    @Query(value = "insert into briefing_dismissal (room_id, account_id, event_type, dismissed_at) values (:roomId, :accountId, :eventType, :at) "
            + "on conflict (room_id, account_id, event_type) do update set dismissed_at = excluded.dismissed_at", nativeQuery = true)
    int upsert(@Param("roomId") Long roomId, @Param("accountId") Long accountId, @Param("eventType") String eventType, @Param("at") java.time.LocalDateTime at);
}
