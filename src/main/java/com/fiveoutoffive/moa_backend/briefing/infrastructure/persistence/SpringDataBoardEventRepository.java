package com.fiveoutoffive.moa_backend.briefing.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataBoardEventRepository extends JpaRepository<BoardEventJpaEntity, Long> {
}
