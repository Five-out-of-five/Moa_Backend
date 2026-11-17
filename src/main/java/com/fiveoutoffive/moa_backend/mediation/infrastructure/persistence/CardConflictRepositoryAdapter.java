package com.fiveoutoffive.moa_backend.mediation.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.mediation.domain.model.CardConflict;
import com.fiveoutoffive.moa_backend.mediation.domain.repository.CardConflictRepository;
import com.fiveoutoffive.moa_backend.mediation.infrastructure.mapper.CardConflictMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
    public Optional<CardConflict> findByIdForUpdate(Long id) {
        return springDataRepository.findByIdForUpdate(id).map(cardConflictMapper::toDomain);
    }

    @Override
    public List<CardConflict> findByRoomId(Long roomId) {
        return springDataRepository.findByRoomId(roomId).stream().map(cardConflictMapper::toDomain).toList();
    }

    @Override
    public Optional<CardConflict> findByCardAIdAndCardBId(Long cardAId, Long cardBId) {
        return springDataRepository.findByCardAIdAndCardBId(cardAId, cardBId).map(cardConflictMapper::toDomain);
    }

    // 감지 핸들러는 트랜잭션 없이 돈다. 쌍마다 따로 커밋해서 한 쌍의 실패가 다른 쌍이나 임베딩을 되돌리지 않게 한다.
    @Override
    @Transactional
    public boolean insertIfAbsent(Long roomId, Long cardAId, Long cardBId, String reason, LocalDateTime detectedAt) {
        return springDataRepository.insertIfAbsent(roomId, cardAId, cardBId, reason, detectedAt) > 0;
    }

    @Override
    public int deleteByCardId(@Param("cardId") Long cardId) {
        return springDataRepository.deleteByCardId(cardId);
    }
}
