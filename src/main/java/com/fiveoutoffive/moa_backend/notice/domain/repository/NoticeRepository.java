package com.fiveoutoffive.moa_backend.notice.domain.repository;

import com.fiveoutoffive.moa_backend.notice.domain.model.Notice;
import com.fiveoutoffive.moa_backend.notice.domain.model.NoticeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface NoticeRepository {

    Notice save(Notice notice);

    Optional<Notice> findById(Long id);

    List<Notice> findActive(@Param("now") LocalDateTime now);

    Page<Notice> findByDeletedAtIsNull(Pageable pageable);

    long countOverlapping(@Param("type") NoticeType type, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("excludeId") Long excludeId);

    List<Notice> findStartingBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
