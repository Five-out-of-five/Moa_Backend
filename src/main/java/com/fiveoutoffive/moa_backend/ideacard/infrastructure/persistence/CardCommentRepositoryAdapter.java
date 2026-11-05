package com.fiveoutoffive.moa_backend.ideacard.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardComment;
import com.fiveoutoffive.moa_backend.ideacard.domain.repository.CardCommentRepository;
import com.fiveoutoffive.moa_backend.ideacard.infrastructure.mapper.CardCommentMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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
}
