package com.fiveoutoffive.moa_backend.vote.domain.repository;

import com.fiveoutoffive.moa_backend.vote.domain.model.VoteBallot;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface VoteBallotRepository {

    VoteBallot save(VoteBallot voteBallot);

    Optional<VoteBallot> findById(Long id);

    List<VoteBallot> findByVoteId(Long voteId);

    List<VoteBallot> findByVoteIdAndAccountId(Long voteId, Long accountId);

    boolean existsByVoteId(Long voteId);

    void deleteByVoteIdAndAccountId(Long voteId, Long accountId);

    void deleteByVoteId(Long voteId);

    List<Long> findVotedIds(@Param("accountId") Long accountId, @Param("voteIds") Collection<Long> voteIds);

    List<Object[]> countParticipants(@Param("voteIds") Collection<Long> voteIds);

    void flush();
}
