package com.fiveoutoffive.moa_backend.session.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.session.domain.model.BoardSession;
import com.fiveoutoffive.moa_backend.session.domain.repository.BoardSessionRepository;
import com.fiveoutoffive.moa_backend.session.infrastructure.mapper.BoardSessionMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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
}
