package com.fiveoutoffive.moa_backend.vote.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.vote.domain.model.VoteOption;
import com.fiveoutoffive.moa_backend.vote.domain.repository.VoteOptionRepository;
import com.fiveoutoffive.moa_backend.vote.infrastructure.mapper.VoteOptionMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class VoteOptionRepositoryAdapter implements VoteOptionRepository {

    private final SpringDataVoteOptionRepository springDataRepository;
    private final VoteOptionMapper voteOptionMapper;

    @Override
    public VoteOption save(VoteOption voteOption) {
        VoteOptionJpaEntity saved = springDataRepository.save(voteOptionMapper.toJpaEntity(voteOption));
        return voteOptionMapper.toDomain(saved);
    }

    @Override
    public Optional<VoteOption> findById(Long id) {
        return springDataRepository.findById(id)
                .map(voteOptionMapper::toDomain);
    }

    @Override
    public List<VoteOption> findByVoteIdOrderBySortOrderAsc(Long voteId) {
        return springDataRepository.findByVoteIdOrderBySortOrderAsc(voteId).stream().map(voteOptionMapper::toDomain).toList();
    }

    @Override
    public void deleteByVoteId(Long voteId) {
        springDataRepository.deleteByVoteId(voteId);
    }

    @Override
    public void flush() {
        springDataRepository.flush();
    }
}
