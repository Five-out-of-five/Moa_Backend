package com.fiveoutoffive.moa_backend.mediation.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataCardConflictRepository extends JpaRepository<CardConflictJpaEntity, Long> {

    List<CardConflictJpaEntity> findByRoomId(Long roomId);

    Optional<CardConflictJpaEntity> findByCardAIdAndCardBId(Long cardAId, Long cardBId);

    /** 행을 잠그고 읽는다. 같은 충돌의 채택·폐기·해제가 동시에 오면 차례로 처리된다. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CardConflictJpaEntity c where c.id = :id")
    Optional<CardConflictJpaEntity> findByIdForUpdate(@Param("id") Long id);

    /** 같은 쌍이 이미 있으면(동시 감지, 해제된 쌍) 아무것도 하지 않고 0 을 반환한다. */
    @Modifying
    @Query(value = "insert into card_conflict (room_id, card_a_id, card_b_id, status, reason, detected_at) "
            + "values (:roomId, :cardAId, :cardBId, 'DETECTED', :reason, :detectedAt) "
            + "on conflict (card_a_id, card_b_id) do nothing", nativeQuery = true)
    int insertIfAbsent(@Param("roomId") Long roomId, @Param("cardAId") Long cardAId, @Param("cardBId") Long cardBId,
                       @Param("reason") String reason, @Param("detectedAt") LocalDateTime detectedAt);

    @Modifying
    @Query("delete from CardConflictJpaEntity c where c.cardAId = :cardId or c.cardBId = :cardId")
    int deleteByCardId(@Param("cardId") Long cardId);
}
