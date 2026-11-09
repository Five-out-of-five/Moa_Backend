package com.fiveoutoffive.moa_backend.mediation.domain.repository;

import com.fiveoutoffive.moa_backend.mediation.domain.model.CardConflict;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface CardConflictRepository {

    CardConflict save(CardConflict cardConflict);

    Optional<CardConflict> findById(Long id);

    List<CardConflict> findByRoomId(Long roomId);

    Optional<CardConflict> findByCardAIdAndCardBId(Long cardAId, Long cardBId);

    int deleteByCardId(@Param("cardId") Long cardId);
}
