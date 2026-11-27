package com.fiveoutoffive.moa_backend.session.domain.repository;

import com.fiveoutoffive.moa_backend.session.domain.model.BoardSession;
import com.fiveoutoffive.moa_backend.session.domain.model.SessionStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface BoardSessionRepository {

    BoardSession save(BoardSession boardSession);

    Optional<BoardSession> findById(Long id);

    Optional<BoardSession> findFirstByRoomIdAndStatus(Long roomId, SessionStatus status);

    Page<BoardSession> findByRoomIdAndStatus(Long roomId, SessionStatus status, Pageable pageable);

    List<BoardSession> findByStatusAndDeadlineAtBefore(SessionStatus status, LocalDateTime time);

    List<BoardSession> findByIdIn(Collection<Long> ids);

    int findMaxSessionNo(@Param("roomId") Long roomId);

    /** 회차 행을 공유 잠금으로 읽는다. 잠근 동안 그 회차는 마감되지 않는다(마감이 기다린다). */
    Optional<BoardSession> findSharedById(Long id);

    /** 방의 진행 중 회차를 공유 잠금으로 읽는다. */
    Optional<BoardSession> findSharedInProgress(Long roomId);

    /** 진행 중인 회차만 마감한다. 실제로 마감했으면 true(동시 마감에서는 한쪽만 true). */
    boolean closeIfInProgress(Long id, LocalDateTime closedAt, Long closedBy);
}
