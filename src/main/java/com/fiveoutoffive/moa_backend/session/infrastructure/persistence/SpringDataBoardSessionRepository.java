package com.fiveoutoffive.moa_backend.session.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
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

    /** 공유 잠금(FOR SHARE). 마감(closeIf)은 이 잠금이 풀릴 때까지 기다린다. 마감과 겹치면 마감된 최신 상태를 돌려준다. */
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select s from BoardSessionJpaEntity s where s.id = :id")
    Optional<BoardSessionJpaEntity> findSharedById(@Param("id") Long id);

    /** 진행 중 회차를 공유 잠금으로 읽는다. 기다리는 사이 마감됐으면 빈 값이다. */
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select s from BoardSessionJpaEntity s where s.roomId = :roomId and s.status = :status")
    Optional<BoardSessionJpaEntity> findSharedByRoomIdAndStatus(@Param("roomId") Long roomId, @Param("status") SessionStatus status);

    /** 진행 중일 때만 마감한다. 동시에 마감하면 한쪽만 1을 받는다(행 잠금 후 조건을 다시 확인). */
    @Modifying(flushAutomatically = true)
    @Query("update BoardSessionJpaEntity s set s.status = :closed, s.closedAt = :closedAt, s.closedByAccountId = :closedBy where s.id = :id and s.status = :inProgress")
    int closeIf(@Param("id") Long id, @Param("closedAt") LocalDateTime closedAt, @Param("closedBy") Long closedBy,
                @Param("closed") SessionStatus closed, @Param("inProgress") SessionStatus inProgress);
}
