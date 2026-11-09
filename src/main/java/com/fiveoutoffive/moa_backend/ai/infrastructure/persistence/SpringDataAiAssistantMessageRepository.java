package com.fiveoutoffive.moa_backend.ai.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataAiAssistantMessageRepository extends JpaRepository<AiAssistantMessageJpaEntity, Long> {

    @Query("select m from AiAssistantMessageJpaEntity m where m.roomId = :roomId and m.accountId = :accountId and (:before is null or m.id < :before) order by m.id desc limit :limit")
    List<AiAssistantMessageJpaEntity> findMine(@Param("roomId") Long roomId, @Param("accountId") Long accountId, @Param("before") Long before, @Param("limit") int limit);
}
