package com.fiveoutoffive.moa_backend.vote.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataVoteRepository extends JpaRepository<VoteJpaEntity, Long> {
}
