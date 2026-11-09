package com.fiveoutoffive.moa_backend.ai.domain.repository;

import com.fiveoutoffive.moa_backend.ai.domain.model.ContentEmbedding;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface ContentEmbeddingRepository {

    ContentEmbedding save(ContentEmbedding contentEmbedding);

    Optional<ContentEmbedding> findById(Long id);

    int upsertCard(@Param("roomId") Long roomId, @Param("cardId") Long cardId, @Param("vector") String vector, @Param("model") String model, @Param("hash") String hash);

    Optional<String> findHashByCardId(@Param("cardId") Long cardId);

    List<Long> findNearestCardIds(@Param("roomId") Long roomId, @Param("cardId") Long cardId, @Param("limit") int limit);

    int deleteByCardId(@Param("cardId") Long cardId);
}
