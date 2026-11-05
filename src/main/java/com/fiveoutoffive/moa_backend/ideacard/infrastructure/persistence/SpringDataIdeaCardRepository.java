package com.fiveoutoffive.moa_backend.ideacard.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataIdeaCardRepository extends JpaRepository<IdeaCardJpaEntity, Long> {
}
