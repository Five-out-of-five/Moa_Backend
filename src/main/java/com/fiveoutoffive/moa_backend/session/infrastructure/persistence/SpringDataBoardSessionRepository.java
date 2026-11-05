package com.fiveoutoffive.moa_backend.session.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataBoardSessionRepository extends JpaRepository<BoardSessionJpaEntity, Long> {
}
