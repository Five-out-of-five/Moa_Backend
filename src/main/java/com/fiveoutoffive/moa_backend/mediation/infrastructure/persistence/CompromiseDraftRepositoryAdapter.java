package com.fiveoutoffive.moa_backend.mediation.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.mediation.domain.model.CompromiseDraft;
import com.fiveoutoffive.moa_backend.mediation.domain.repository.CompromiseDraftRepository;
import com.fiveoutoffive.moa_backend.mediation.infrastructure.mapper.CompromiseDraftMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CompromiseDraftRepositoryAdapter implements CompromiseDraftRepository {

    private final SpringDataCompromiseDraftRepository springDataRepository;
    private final CompromiseDraftMapper compromiseDraftMapper;

    @Override
    public CompromiseDraft save(CompromiseDraft compromiseDraft) {
        CompromiseDraftJpaEntity saved = springDataRepository.save(compromiseDraftMapper.toJpaEntity(compromiseDraft));
        return compromiseDraftMapper.toDomain(saved);
    }

    @Override
    public Optional<CompromiseDraft> findById(Long id) {
        return springDataRepository.findById(id)
                .map(compromiseDraftMapper::toDomain);
    }
}
