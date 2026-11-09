package com.fiveoutoffive.moa_backend.vote.domain.repository;

import com.fiveoutoffive.moa_backend.vote.domain.model.Vote;
import com.fiveoutoffive.moa_backend.vote.domain.model.VoteStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface VoteRepository {

    Vote save(Vote vote);

    Optional<Vote> findById(Long id);

    List<Vote> findBySourceCardIdInAndDeletedAtIsNull(Collection<Long> cardIds);

    Optional<Vote> findFirstBySourceCardIdAndStatusAndDeletedAtIsNull(Long cardId, VoteStatus status);

    Page<Vote> findByRoomIdAndDeletedAtIsNull(Long roomId, Pageable pageable);

    Page<Vote> findByRoomIdAndStatusAndDeletedAtIsNull(Long roomId, VoteStatus status, Pageable pageable);

    List<Vote> findByRoomIdAndStatusAndDeletedAtIsNull(Long roomId, VoteStatus status);

    List<Vote> findByStatusAndDeadlineAtBeforeAndDeletedAtIsNull(VoteStatus status, LocalDateTime time);

    List<Vote> findByStatusAndReminderSentAtIsNullAndDeadlineAtBeforeAndDeletedAtIsNull(VoteStatus status, LocalDateTime time);
}
