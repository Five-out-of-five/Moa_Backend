package com.fiveoutoffive.moa_backend.notice.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataNoticeDismissalRepository extends JpaRepository<NoticeDismissalJpaEntity, NoticeDismissalJpaId> {

    List<NoticeDismissalJpaEntity> findByAccountId(Long accountId);
}
