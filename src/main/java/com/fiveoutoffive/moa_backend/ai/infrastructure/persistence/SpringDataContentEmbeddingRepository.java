package com.fiveoutoffive.moa_backend.ai.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataContentEmbeddingRepository extends JpaRepository<ContentEmbeddingJpaEntity, Long> {

    @Modifying
    @Query(value = "insert into content_embedding (room_id, card_id, embedding, model, source_hash, updated_at) values (:roomId, :cardId, cast(:vector as vector), :model, :hash, (CURRENT_TIMESTAMP AT TIME ZONE 'UTC')) on conflict (card_id) where card_id is not null do update set embedding = excluded.embedding, model = excluded.model, source_hash = excluded.source_hash, updated_at = excluded.updated_at", nativeQuery = true)
    int upsertCard(@Param("roomId") Long roomId, @Param("cardId") Long cardId, @Param("vector") String vector, @Param("model") String model, @Param("hash") String hash);

    @Query(value = "select source_hash from content_embedding where card_id = :cardId", nativeQuery = true)
    Optional<String> findHashByCardId(@Param("cardId") Long cardId);

    @Query(value = "select e.card_id from content_embedding e join idea_card c on c.id = e.card_id where e.room_id = :roomId and c.deleted_at is null and e.card_id <> :cardId order by e.embedding <=> (select embedding from content_embedding where card_id = :cardId) limit :limit", nativeQuery = true)
    List<Long> findNearestCardIds(@Param("roomId") Long roomId, @Param("cardId") Long cardId, @Param("limit") int limit);

    @Modifying
    @Query(value = "delete from content_embedding where card_id = :cardId", nativeQuery = true)
    int deleteByCardId(@Param("cardId") Long cardId);
}
