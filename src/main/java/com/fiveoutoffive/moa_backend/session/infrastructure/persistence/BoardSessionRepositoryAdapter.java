package com.fiveoutoffive.moa_backend.session.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.session.domain.model.BoardSession;
import com.fiveoutoffive.moa_backend.session.domain.model.SessionStatus;
import com.fiveoutoffive.moa_backend.session.domain.repository.BoardSessionRepository;
import com.fiveoutoffive.moa_backend.session.infrastructure.mapper.BoardSessionMapper;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BoardSessionRepositoryAdapter implements BoardSessionRepository {

    private final SpringDataBoardSessionRepository springDataRepository;
    private final BoardSessionMapper boardSessionMapper;

    @Override
    public BoardSession save(BoardSession boardSession) {
        BoardSessionJpaEntity saved = springDataRepository.save(boardSessionMapper.toJpaEntity(boardSession));
        return boardSessionMapper.toDomain(saved);
    }

    @Override
    public Optional<BoardSession> findById(Long id) {
        return springDataRepository.findById(id)
                .map(boardSessionMapper::toDomain);
    }

    @Override
    public Optional<BoardSession> findFirstByRoomIdAndStatus(Long roomId, SessionStatus status) {
        return springDataRepository.findFirstByRoomIdAndStatus(roomId, status).map(boardSessionMapper::toDomain);
    }

    @Override
    public Page<BoardSession> findByRoomIdAndStatus(Long roomId, SessionStatus status, Pageable pageable) {
        return springDataRepository.findByRoomIdAndStatus(roomId, status, pageable).map(boardSessionMapper::toDomain);
    }

    @Override
    public List<BoardSession> findByStatusAndDeadlineAtBefore(SessionStatus status, LocalDateTime time) {
        return springDataRepository.findByStatusAndDeadlineAtBefore(status, time).stream().map(boardSessionMapper::toDomain).toList();
    }

    @Override
    public List<BoardSession> findByIdIn(Collection<Long> ids) {
        return springDataRepository.findByIdIn(ids).stream().map(boardSessionMapper::toDomain).toList();
    }

    @Override
    public int findMaxSessionNo(@Param("roomId") Long roomId) {
        return springDataRepository.findMaxSessionNo(roomId);
    }
}
