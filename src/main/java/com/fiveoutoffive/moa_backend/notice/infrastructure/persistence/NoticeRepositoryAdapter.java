package com.fiveoutoffive.moa_backend.notice.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.notice.domain.model.Notice;
import com.fiveoutoffive.moa_backend.notice.domain.repository.NoticeRepository;
import com.fiveoutoffive.moa_backend.notice.infrastructure.mapper.NoticeMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NoticeRepositoryAdapter implements NoticeRepository {

    private final SpringDataNoticeRepository springDataRepository;
    private final NoticeMapper noticeMapper;

    @Override
    public Notice save(Notice notice) {
        NoticeJpaEntity saved = springDataRepository.save(noticeMapper.toJpaEntity(notice));
        return noticeMapper.toDomain(saved);
    }

    @Override
    public Optional<Notice> findById(Long id) {
        return springDataRepository.findById(id)
                .map(noticeMapper::toDomain);
    }
}
