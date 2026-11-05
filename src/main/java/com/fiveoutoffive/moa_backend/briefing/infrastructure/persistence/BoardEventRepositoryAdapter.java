package com.fiveoutoffive.moa_backend.briefing.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.briefing.domain.model.BoardEvent;
import com.fiveoutoffive.moa_backend.briefing.domain.repository.BoardEventRepository;
import com.fiveoutoffive.moa_backend.briefing.infrastructure.mapper.BoardEventMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BoardEventRepositoryAdapter implements BoardEventRepository {

    private final SpringDataBoardEventRepository springDataRepository;
    private final BoardEventMapper boardEventMapper;

    @Override
    public BoardEvent save(BoardEvent boardEvent) {
        BoardEventJpaEntity saved = springDataRepository.save(boardEventMapper.toJpaEntity(boardEvent));
        return boardEventMapper.toDomain(saved);
    }

    @Override
    public Optional<BoardEvent> findById(Long id) {
        return springDataRepository.findById(id)
                .map(boardEventMapper::toDomain);
    }
}
