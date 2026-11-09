package com.fiveoutoffive.moa_backend.briefing.domain.repository;

import com.fiveoutoffive.moa_backend.briefing.domain.model.BoardEvent;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface BoardEventRepository {

    BoardEvent save(BoardEvent boardEvent);

    Optional<BoardEvent> findById(Long id);

    List<BoardEvent> findRecent(@Param("roomId") Long roomId, @Param("since") LocalDateTime since, @Param("limit") LocalDateTime limit);

    int deleteOlderThan(@Param("before") LocalDateTime before);
}
