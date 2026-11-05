package com.fiveoutoffive.moa_backend.mediation.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.mediation.domain.model.CardConflict;
import com.fiveoutoffive.moa_backend.mediation.domain.repository.CardConflictRepository;
import com.fiveoutoffive.moa_backend.mediation.infrastructure.mapper.CardConflictMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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
}
