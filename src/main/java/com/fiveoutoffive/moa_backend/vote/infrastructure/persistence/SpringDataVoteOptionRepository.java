package com.fiveoutoffive.moa_backend.vote.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataVoteOptionRepository extends JpaRepository<VoteOptionJpaEntity, Long> {

    List<VoteOptionJpaEntity> findByVoteIdOrderBySortOrderAsc(Long voteId);

    void deleteByVoteId(Long voteId);
}
