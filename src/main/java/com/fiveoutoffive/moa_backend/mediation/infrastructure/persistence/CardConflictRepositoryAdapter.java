package com.fiveoutoffive.moa_backend.mediation.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.mediation.domain.model.CardConflict;
import com.fiveoutoffive.moa_backend.mediation.domain.repository.CardConflictRepository;
import com.fiveoutoffive.moa_backend.mediation.infrastructure.mapper.CardConflictMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CardConflictRepositoryAdapter implements CardConflictRepository {

    private final SpringDataCardConflictRepository springDataRepository;
    private final CardConflictMapper cardConflictMapper;

    @Override
    public CardConflict save(CardConflict cardConflict) {
        CardConflictJpaEntity saved = springDataRepository.save(cardConflictMapper.toJpaEntity(cardConflict));
        return cardConflictMapper.toDomain(saved);
    }

    @Override
    public Optional<CardConflict> findById(Long id) {
        return springDataRepository.findById(id)
                .map(cardConflictMapper::toDomain);
    }

    @Override
    public List<CardConflict> findByRoomId(Long roomId) {
        return springDataRepository.findByRoomId(roomId).stream().map(cardConflictMapper::toDomain).toList();
    }

    @Override
    public Optional<CardConflict> findByCardAIdAndCardBId(Long cardAId, Long cardBId) {
        return springDataRepository.findByCardAIdAndCardBId(cardAId, cardBId).map(cardConflictMapper::toDomain);
    }

    @Override
    public int deleteByCardId(@Param("cardId") Long cardId) {
        return springDataRepository.deleteByCardId(cardId);
    }
}
