package com.fiveoutoffive.moa_backend.vote.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.vote.domain.model.VoteStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataVoteRepository extends JpaRepository<VoteJpaEntity, Long> {

    List<VoteJpaEntity> findBySourceCardIdInAndDeletedAtIsNull(Collection<Long> cardIds);

    Optional<VoteJpaEntity> findFirstBySourceCardIdAndStatusAndDeletedAtIsNull(Long cardId, VoteStatus status);

    Page<VoteJpaEntity> findByRoomIdAndDeletedAtIsNull(Long roomId, Pageable pageable);

    Page<VoteJpaEntity> findByRoomIdAndStatusAndDeletedAtIsNull(Long roomId, VoteStatus status, Pageable pageable);

    List<VoteJpaEntity> findByRoomIdAndStatusAndDeletedAtIsNull(Long roomId, VoteStatus status);

    List<VoteJpaEntity> findByStatusAndDeadlineAtBeforeAndDeletedAtIsNull(VoteStatus status, LocalDateTime time);

    List<VoteJpaEntity> findByStatusAndReminderSentAtIsNullAndDeadlineAtBeforeAndDeletedAtIsNull(VoteStatus status, LocalDateTime time);
}
