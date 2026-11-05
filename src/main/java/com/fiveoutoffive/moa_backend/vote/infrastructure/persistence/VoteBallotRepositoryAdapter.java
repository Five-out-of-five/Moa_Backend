package com.fiveoutoffive.moa_backend.vote.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.vote.domain.model.VoteBallot;
import com.fiveoutoffive.moa_backend.vote.domain.repository.VoteBallotRepository;
import com.fiveoutoffive.moa_backend.vote.infrastructure.mapper.VoteBallotMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class VoteBallotRepositoryAdapter implements VoteBallotRepository {

    private final SpringDataVoteBallotRepository springDataRepository;
    private final VoteBallotMapper voteBallotMapper;

    @Override
    public VoteBallot save(VoteBallot voteBallot) {
        VoteBallotJpaEntity saved = springDataRepository.save(voteBallotMapper.toJpaEntity(voteBallot));
        return voteBallotMapper.toDomain(saved);
    }

    @Override
    public Optional<VoteBallot> findById(Long id) {
        return springDataRepository.findById(id)
                .map(voteBallotMapper::toDomain);
    }
}
