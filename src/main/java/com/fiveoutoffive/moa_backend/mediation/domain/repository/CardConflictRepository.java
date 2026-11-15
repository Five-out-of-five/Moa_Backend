package com.fiveoutoffive.moa_backend.mediation.domain.repository;

import com.fiveoutoffive.moa_backend.mediation.domain.model.CardConflict;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface CardConflictRepository {

    CardConflict save(CardConflict cardConflict);

    Optional<CardConflict> findById(Long id);

    /** 행 잠금(트랜잭션 끝까지)을 걸고 읽는다. 트랜잭션 안에서만 쓴다. */
    Optional<CardConflict> findByIdForUpdate(Long id);

    List<CardConflict> findByRoomId(Long roomId);

    Optional<CardConflict> findByCardAIdAndCardBId(Long cardAId, Long cardBId);

    /** 감지된 쌍을 저장한다. 같은 쌍이 이미 있으면 저장하지 않고 false. 결과는 바로 커밋된다. */
    boolean insertIfAbsent(Long roomId, Long cardAId, Long cardBId, String reason, LocalDateTime detectedAt);

    int deleteByCardId(@Param("cardId") Long cardId);
}
