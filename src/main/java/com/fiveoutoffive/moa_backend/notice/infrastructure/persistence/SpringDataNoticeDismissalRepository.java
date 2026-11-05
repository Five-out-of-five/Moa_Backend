package com.fiveoutoffive.moa_backend.notice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataNoticeDismissalRepository extends JpaRepository<NoticeDismissalJpaEntity, NoticeDismissalJpaId> {
}
