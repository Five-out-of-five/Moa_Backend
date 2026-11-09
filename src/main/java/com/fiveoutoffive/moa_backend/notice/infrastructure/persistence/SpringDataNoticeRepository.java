package com.fiveoutoffive.moa_backend.notice.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.notice.domain.model.NoticeType;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataNoticeRepository extends JpaRepository<NoticeJpaEntity, Long> {

    @Query("select n from NoticeJpaEntity n where n.deletedAt is null and n.displayStartsAt <= :now and n.displayEndsAt > :now order by n.displayStartsAt desc")
    List<NoticeJpaEntity> findActive(@Param("now") LocalDateTime now);

    Page<NoticeJpaEntity> findByDeletedAtIsNull(Pageable pageable);

    @Query("select count(n) from NoticeJpaEntity n where n.deletedAt is null and n.noticeType = :type and n.displayStartsAt < :end and n.displayEndsAt > :start and (:excludeId is null or n.id <> :excludeId)")
    long countOverlapping(@Param("type") NoticeType type, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("excludeId") Long excludeId);

    @Query("select n from NoticeJpaEntity n where n.deletedAt is null and n.displayStartsAt > :from and n.displayStartsAt <= :to")
    List<NoticeJpaEntity> findStartingBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
