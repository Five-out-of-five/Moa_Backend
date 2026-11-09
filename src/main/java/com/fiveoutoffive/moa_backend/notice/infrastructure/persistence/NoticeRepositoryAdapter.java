package com.fiveoutoffive.moa_backend.notice.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.notice.domain.model.Notice;
import com.fiveoutoffive.moa_backend.notice.domain.model.NoticeType;
import com.fiveoutoffive.moa_backend.notice.domain.repository.NoticeRepository;
import com.fiveoutoffive.moa_backend.notice.infrastructure.mapper.NoticeMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
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

    @Override
    public List<Notice> findActive(@Param("now") LocalDateTime now) {
        return springDataRepository.findActive(now).stream().map(noticeMapper::toDomain).toList();
    }

    @Override
    public Page<Notice> findByDeletedAtIsNull(Pageable pageable) {
        return springDataRepository.findByDeletedAtIsNull(pageable).map(noticeMapper::toDomain);
    }

    @Override
    public long countOverlapping(@Param("type") NoticeType type, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("excludeId") Long excludeId) {
        return springDataRepository.countOverlapping(type, start, end, excludeId);
    }

    @Override
    public List<Notice> findStartingBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to) {
        return springDataRepository.findStartingBetween(from, to).stream().map(noticeMapper::toDomain).toList();
    }
}
