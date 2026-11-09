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
}
