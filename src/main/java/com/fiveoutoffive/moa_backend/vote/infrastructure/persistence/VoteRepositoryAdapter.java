package com.fiveoutoffive.moa_backend.vote.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.vote.domain.model.Vote;
import com.fiveoutoffive.moa_backend.vote.domain.repository.VoteRepository;
import com.fiveoutoffive.moa_backend.vote.infrastructure.mapper.VoteMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class VoteRepositoryAdapter implements VoteRepository {

    private final SpringDataVoteRepository springDataRepository;
    private final VoteMapper voteMapper;

    @Override
    public Vote save(Vote vote) {
        VoteJpaEntity saved = springDataRepository.save(voteMapper.toJpaEntity(vote));
        return voteMapper.toDomain(saved);
    }

    @Override
    public Optional<Vote> findById(Long id) {
        return springDataRepository.findById(id)
                .map(voteMapper::toDomain);
    }
}
