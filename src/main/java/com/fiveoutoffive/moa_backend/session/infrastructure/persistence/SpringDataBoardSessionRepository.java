package com.fiveoutoffive.moa_backend.session.infrastructure.persistence;

import org.springframework.data.jpa.repository.Modifying;
import com.fiveoutoffive.moa_backend.session.domain.model.SessionStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataBoardSessionRepository extends JpaRepository<BoardSessionJpaEntity, Long> {

    Optional<BoardSessionJpaEntity> findFirstByRoomIdAndStatus(Long roomId, SessionStatus status);

    Page<BoardSessionJpaEntity> findByRoomIdAndStatus(Long roomId, SessionStatus status, Pageable pageable);

    List<BoardSessionJpaEntity> findByStatusAndDeadlineAtBefore(SessionStatus status, LocalDateTime time);

    List<BoardSessionJpaEntity> findByIdIn(Collection<Long> ids);

    @Query("select coalesce(max(s.sessionNo), 0) from BoardSessionJpaEntity s where s.roomId = :roomId")
    int findMaxSessionNo(@Param("roomId") Long roomId);

    /** 진행 중일 때만 마감한다. 동시에 마감하면 한쪽만 1을 받는다(행 잠금 후 조건을 다시 확인). */
    @Modifying(flushAutomatically = true)
    @Query("update BoardSessionJpaEntity s set s.status = :closed, s.closedAt = :closedAt, s.closedByAccountId = :closedBy where s.id = :id and s.status = :inProgress")
    int closeIf(@Param("id") Long id, @Param("closedAt") LocalDateTime closedAt, @Param("closedBy") Long closedBy,
                @Param("closed") SessionStatus closed, @Param("inProgress") SessionStatus inProgress);
}
