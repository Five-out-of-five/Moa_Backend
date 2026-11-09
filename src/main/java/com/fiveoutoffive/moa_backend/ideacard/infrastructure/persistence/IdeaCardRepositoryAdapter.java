package com.fiveoutoffive.moa_backend.ideacard.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardType;
import com.fiveoutoffive.moa_backend.ideacard.domain.model.IdeaCard;
import com.fiveoutoffive.moa_backend.ideacard.domain.repository.IdeaCardRepository;
import com.fiveoutoffive.moa_backend.ideacard.infrastructure.mapper.IdeaCardMapper;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class IdeaCardRepositoryAdapter implements IdeaCardRepository {

    private final SpringDataIdeaCardRepository springDataRepository;
    private final IdeaCardMapper ideaCardMapper;

    @Override
    public IdeaCard save(IdeaCard ideaCard) {
        IdeaCardJpaEntity saved = springDataRepository.save(ideaCardMapper.toJpaEntity(ideaCard));
        return ideaCardMapper.toDomain(saved);
    }

    @Override
    public Optional<IdeaCard> findById(Long id) {
        return springDataRepository.findById(id)
                .map(ideaCardMapper::toDomain);
    }

    @Override
    public List<IdeaCard> findBySessionIdAndDeletedAtIsNullOrderBySortOrderAsc(Long sessionId) {
        return springDataRepository.findBySessionIdAndDeletedAtIsNullOrderBySortOrderAsc(sessionId).stream().map(ideaCardMapper::toDomain).toList();
    }

    @Override
    public List<IdeaCard> findBySessionIdAndCardTypeAndDeletedAtIsNullOrderBySortOrderAsc(Long sessionId, CardType cardType) {
        return springDataRepository.findBySessionIdAndCardTypeAndDeletedAtIsNullOrderBySortOrderAsc(sessionId, cardType).stream().map(ideaCardMapper::toDomain).toList();
    }

    @Override
    public long countBySessionIdAndDeletedAtIsNull(Long sessionId) {
        return springDataRepository.countBySessionIdAndDeletedAtIsNull(sessionId);
    }

    @Override
    public long countByRoomIdAndSessionIdIn(Long roomId, Collection<Long> sessionIds) {
        return springDataRepository.countByRoomIdAndSessionIdIn(roomId, sessionIds);
    }

    @Override
    public List<IdeaCard> findByRoomIdAndDeletedAtIsNull(Long roomId) {
        return springDataRepository.findByRoomIdAndDeletedAtIsNull(roomId).stream().map(ideaCardMapper::toDomain).toList();
    }

    @Override
    public List<IdeaCard> findByRoomIdAndSessionIdInAndDeletedAtIsNull(Long roomId, Collection<Long> sessionIds) {
        return springDataRepository.findByRoomIdAndSessionIdInAndDeletedAtIsNull(roomId, sessionIds).stream().map(ideaCardMapper::toDomain).toList();
    }

    @Override
    public List<IdeaCard> findByIdIn(Collection<Long> ids) {
        return springDataRepository.findByIdIn(ids).stream().map(ideaCardMapper::toDomain).toList();
    }

    @Override
    public long countWritersBySessionId(@Param("sessionId") Long sessionId) {
        return springDataRepository.countWritersBySessionId(sessionId);
    }

    @Override
    public int findMaxSortOrder(@Param("sessionId") Long sessionId, @Param("cardType") CardType cardType) {
        return springDataRepository.findMaxSortOrder(sessionId, cardType);
    }
}
