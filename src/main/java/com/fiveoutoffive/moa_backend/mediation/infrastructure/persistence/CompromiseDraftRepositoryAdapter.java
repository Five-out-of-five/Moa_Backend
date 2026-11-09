package com.fiveoutoffive.moa_backend.mediation.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.mediation.domain.model.CompromiseDraft;
import com.fiveoutoffive.moa_backend.mediation.domain.model.DraftStatus;
import com.fiveoutoffive.moa_backend.mediation.domain.repository.CompromiseDraftRepository;
import com.fiveoutoffive.moa_backend.mediation.infrastructure.mapper.CompromiseDraftMapper;
import java.util.List;
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

    @Override
    public List<CompromiseDraft> findByConflictIdAndRequestedByAccountIdAndStatus(Long conflictId, Long requestedByAccountId, DraftStatus status) {
        return springDataRepository.findByConflictIdAndRequestedByAccountIdAndStatus(conflictId, requestedByAccountId, status).stream().map(compromiseDraftMapper::toDomain).toList();
    }
}
