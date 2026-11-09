package com.fiveoutoffive.moa_backend.ideacard.domain.repository;

import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardFile;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface CardFileRepository {

    CardFile save(CardFile cardFile);

    Optional<CardFile> findById(Long id);

    List<CardFile> findByCardIdOrderBySortOrderAsc(Long cardId);

    List<CardFile> findByCardIdIn(Collection<Long> cardIds);

    Optional<CardFile> findByCardIdAndFileId(Long cardId, Long fileId);

    void deleteById(Long id);
}
