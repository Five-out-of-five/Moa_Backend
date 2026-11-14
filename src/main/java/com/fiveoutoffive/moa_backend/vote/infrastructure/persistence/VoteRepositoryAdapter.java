package com.fiveoutoffive.moa_backend.vote.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.vote.domain.model.Vote;
import com.fiveoutoffive.moa_backend.vote.domain.model.VoteStatus;
import com.fiveoutoffive.moa_backend.vote.domain.repository.VoteRepository;
import com.fiveoutoffive.moa_backend.vote.infrastructure.mapper.VoteMapper;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class VoteRepositoryAdapter implements VoteRepository {

    private final SpringDataVoteRepository springDataRepository;
    private final VoteMapper voteMapper;

    @Override
    public Vote save(Vote vote) {
        // flush 해야 @Version 이 올라간 값이 엔티티에 반영된다. 그 전에 변환하면 응답에 이전 version 이 나간다.
        VoteJpaEntity saved = springDataRepository.saveAndFlush(voteMapper.toJpaEntity(vote));
        return voteMapper.toDomain(saved);
    }

    @Override
    public Optional<Vote> findById(Long id) {
        return springDataRepository.findById(id)
                .map(voteMapper::toDomain);
    }

    @Override
    public List<Vote> findBySourceCardIdInAndDeletedAtIsNull(Collection<Long> cardIds) {
        return springDataRepository.findBySourceCardIdInAndDeletedAtIsNull(cardIds).stream().map(voteMapper::toDomain).toList();
    }

    @Override
    public Optional<Vote> findFirstBySourceCardIdAndStatusAndDeletedAtIsNull(Long cardId, VoteStatus status) {
        return springDataRepository.findFirstBySourceCardIdAndStatusAndDeletedAtIsNull(cardId, status).map(voteMapper::toDomain);
    }

    @Override
    public Page<Vote> findByRoomIdAndDeletedAtIsNull(Long roomId, Pageable pageable) {
        return springDataRepository.findByRoomIdAndDeletedAtIsNull(roomId, pageable).map(voteMapper::toDomain);
    }

    @Override
    public Page<Vote> findByRoomIdAndStatusAndDeletedAtIsNull(Long roomId, VoteStatus status, Pageable pageable) {
        return springDataRepository.findByRoomIdAndStatusAndDeletedAtIsNull(roomId, status, pageable).map(voteMapper::toDomain);
    }

    @Override
    public List<Vote> findByRoomIdAndStatusAndDeletedAtIsNull(Long roomId, VoteStatus status) {
        return springDataRepository.findByRoomIdAndStatusAndDeletedAtIsNull(roomId, status).stream().map(voteMapper::toDomain).toList();
    }

    @Override
    public List<Vote> findByStatusAndDeadlineAtBeforeAndDeletedAtIsNull(VoteStatus status, LocalDateTime time) {
        return springDataRepository.findByStatusAndDeadlineAtBeforeAndDeletedAtIsNull(status, time).stream().map(voteMapper::toDomain).toList();
    }

    @Override
    public List<Vote> findByStatusAndReminderSentAtIsNullAndDeadlineAtBeforeAndDeletedAtIsNull(VoteStatus status, LocalDateTime time) {
        return springDataRepository.findByStatusAndReminderSentAtIsNullAndDeadlineAtBeforeAndDeletedAtIsNull(status, time).stream().map(voteMapper::toDomain).toList();
    }
}
