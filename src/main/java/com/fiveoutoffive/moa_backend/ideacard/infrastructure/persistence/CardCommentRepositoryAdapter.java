package com.fiveoutoffive.moa_backend.ideacard.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardComment;
import com.fiveoutoffive.moa_backend.ideacard.domain.repository.CardCommentRepository;
import com.fiveoutoffive.moa_backend.ideacard.infrastructure.mapper.CardCommentMapper;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CardCommentRepositoryAdapter implements CardCommentRepository {

    private final SpringDataCardCommentRepository springDataRepository;
    private final CardCommentMapper cardCommentMapper;

    @Override
    public CardComment save(CardComment cardComment) {
        CardCommentJpaEntity saved = springDataRepository.save(cardCommentMapper.toJpaEntity(cardComment));
        return cardCommentMapper.toDomain(saved);
    }

    @Override
    public Optional<CardComment> findById(Long id) {
        return springDataRepository.findById(id)
                .map(cardCommentMapper::toDomain);
    }

    @Override
    public List<CardComment> findByCardIdOrderByCreatedAtAsc(Long cardId) {
        return springDataRepository.findByCardIdOrderByCreatedAtAsc(cardId).stream().map(cardCommentMapper::toDomain).toList();
    }

    @Override
    public List<CardComment> findByCardIdAndDeletedAtIsNull(Long cardId) {
        return springDataRepository.findByCardIdAndDeletedAtIsNull(cardId).stream().map(cardCommentMapper::toDomain).toList();
    }

    @Override
    public List<Object[]> countByCardIds(@Param("ids") Collection<Long> ids) {
        return springDataRepository.countByCardIds(ids);
    }
}
