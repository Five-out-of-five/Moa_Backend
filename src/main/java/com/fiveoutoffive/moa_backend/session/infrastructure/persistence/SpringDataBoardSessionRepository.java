package com.fiveoutoffive.moa_backend.session.infrastructure.persistence;

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
}
