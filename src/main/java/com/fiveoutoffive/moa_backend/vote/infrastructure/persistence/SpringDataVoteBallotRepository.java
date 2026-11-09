package com.fiveoutoffive.moa_backend.vote.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataVoteBallotRepository extends JpaRepository<VoteBallotJpaEntity, Long> {

    List<VoteBallotJpaEntity> findByVoteId(Long voteId);

    List<VoteBallotJpaEntity> findByVoteIdAndAccountId(Long voteId, Long accountId);

    boolean existsByVoteId(Long voteId);

    void deleteByVoteIdAndAccountId(Long voteId, Long accountId);

    void deleteByVoteId(Long voteId);

    @Query("select distinct b.voteId from VoteBallotJpaEntity b where b.accountId = :accountId and b.voteId in :voteIds")
    List<Long> findVotedIds(@Param("accountId") Long accountId, @Param("voteIds") Collection<Long> voteIds);

    @Query("select b.voteId, count(distinct b.accountId) from VoteBallotJpaEntity b where b.voteId in :voteIds group by b.voteId")
    List<Object[]> countParticipants(@Param("voteIds") Collection<Long> voteIds);
}
