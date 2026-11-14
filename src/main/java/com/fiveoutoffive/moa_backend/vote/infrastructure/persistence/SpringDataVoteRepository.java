package com.fiveoutoffive.moa_backend.vote.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.vote.domain.model.VoteStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataVoteRepository extends JpaRepository<VoteJpaEntity, Long> {

    // 이후 같은 트랜잭션에서 투표를 다시 읽을 때 올라간 version 을 보도록 영속성 컨텍스트를 비운다.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update VoteJpaEntity v set v.version = v.version + 1 where v.id = :id")
    int incrementVersion(@Param("id") Long id);

    List<VoteJpaEntity> findBySourceCardIdInAndDeletedAtIsNull(Collection<Long> cardIds);

    Optional<VoteJpaEntity> findFirstBySourceCardIdAndStatusAndDeletedAtIsNull(Long cardId, VoteStatus status);

    Page<VoteJpaEntity> findByRoomIdAndDeletedAtIsNull(Long roomId, Pageable pageable);

    Page<VoteJpaEntity> findByRoomIdAndStatusAndDeletedAtIsNull(Long roomId, VoteStatus status, Pageable pageable);

    List<VoteJpaEntity> findByRoomIdAndStatusAndDeletedAtIsNull(Long roomId, VoteStatus status);

    List<VoteJpaEntity> findByStatusAndDeadlineAtBeforeAndDeletedAtIsNull(VoteStatus status, LocalDateTime time);

    List<VoteJpaEntity> findByStatusAndReminderSentAtIsNullAndDeadlineAtBeforeAndDeletedAtIsNull(VoteStatus status, LocalDateTime time);
}
