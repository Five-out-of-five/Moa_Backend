package com.fiveoutoffive.moa_backend.briefing.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataBoardEventRepository extends JpaRepository<BoardEventJpaEntity, Long> {

    @Query("select e from BoardEventJpaEntity e where e.roomId = :roomId and e.createdAt > :since and e.createdAt >= :limit order by e.createdAt desc")
    List<BoardEventJpaEntity> findRecent(@Param("roomId") Long roomId, @Param("since") LocalDateTime since, @Param("limit") LocalDateTime limit);

    @Modifying
    @Query("delete from BoardEventJpaEntity e where e.createdAt < :before")
    int deleteOlderThan(@Param("before") LocalDateTime before);
}
