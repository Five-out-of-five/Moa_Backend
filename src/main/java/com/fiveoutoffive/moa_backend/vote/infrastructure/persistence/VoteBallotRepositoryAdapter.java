package com.fiveoutoffive.moa_backend.vote.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.vote.domain.model.VoteBallot;
import com.fiveoutoffive.moa_backend.vote.domain.repository.VoteBallotRepository;
import com.fiveoutoffive.moa_backend.vote.infrastructure.mapper.VoteBallotMapper;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
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

    @Override
    public List<VoteBallot> findByVoteId(Long voteId) {
        return springDataRepository.findByVoteId(voteId).stream().map(voteBallotMapper::toDomain).toList();
    }

    @Override
    public List<VoteBallot> findByVoteIdAndAccountId(Long voteId, Long accountId) {
        return springDataRepository.findByVoteIdAndAccountId(voteId, accountId).stream().map(voteBallotMapper::toDomain).toList();
    }

    @Override
    public boolean existsByVoteId(Long voteId) {
        return springDataRepository.existsByVoteId(voteId);
    }

    @Override
    public void deleteByVoteIdAndAccountId(Long voteId, Long accountId) {
        springDataRepository.deleteByVoteIdAndAccountId(voteId, accountId);
    }

    @Override
    public void deleteByVoteId(Long voteId) {
        springDataRepository.deleteByVoteId(voteId);
    }

    @Override
    public List<Long> findVotedIds(@Param("accountId") Long accountId, @Param("voteIds") Collection<Long> voteIds) {
        return springDataRepository.findVotedIds(accountId, voteIds);
    }

    @Override
    public List<Object[]> countParticipants(@Param("voteIds") Collection<Long> voteIds) {
        return springDataRepository.countParticipants(voteIds);
    }

    @Override
    public void flush() {
        springDataRepository.flush();
    }
}
