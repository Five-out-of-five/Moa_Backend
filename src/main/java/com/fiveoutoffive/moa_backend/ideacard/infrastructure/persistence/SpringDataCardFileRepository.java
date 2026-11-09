package com.fiveoutoffive.moa_backend.ideacard.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCardFileRepository extends JpaRepository<CardFileJpaEntity, Long> {

    List<CardFileJpaEntity> findByCardIdOrderBySortOrderAsc(Long cardId);

    List<CardFileJpaEntity> findByCardIdIn(Collection<Long> cardIds);

    Optional<CardFileJpaEntity> findByCardIdAndFileId(Long cardId, Long fileId);

    void deleteById(Long id);
}
