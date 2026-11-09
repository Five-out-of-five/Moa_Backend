package com.fiveoutoffive.moa_backend.briefing.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.briefing.domain.model.BoardEvent;
import com.fiveoutoffive.moa_backend.briefing.domain.repository.BoardEventRepository;
import com.fiveoutoffive.moa_backend.briefing.infrastructure.mapper.BoardEventMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
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

    @Override
    public List<BoardEvent> findRecent(@Param("roomId") Long roomId, @Param("since") LocalDateTime since, @Param("limit") LocalDateTime limit) {
        return springDataRepository.findRecent(roomId, since, limit).stream().map(boardEventMapper::toDomain).toList();
    }

    @Override
    public int deleteOlderThan(@Param("before") LocalDateTime before) {
        return springDataRepository.deleteOlderThan(before);
    }
}
