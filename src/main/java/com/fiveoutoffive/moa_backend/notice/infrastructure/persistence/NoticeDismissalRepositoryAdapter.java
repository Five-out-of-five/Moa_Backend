package com.fiveoutoffive.moa_backend.notice.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.notice.domain.model.NoticeDismissal;
import com.fiveoutoffive.moa_backend.notice.domain.repository.NoticeDismissalRepository;
import com.fiveoutoffive.moa_backend.notice.infrastructure.mapper.NoticeDismissalMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NoticeDismissalRepositoryAdapter implements NoticeDismissalRepository {

    private final SpringDataNoticeDismissalRepository springDataRepository;
    private final NoticeDismissalMapper noticeDismissalMapper;

    @Override
    public NoticeDismissal save(NoticeDismissal noticeDismissal) {
        NoticeDismissalJpaEntity saved = springDataRepository.save(noticeDismissalMapper.toJpaEntity(noticeDismissal));
        return noticeDismissalMapper.toDomain(saved);
    }
}
