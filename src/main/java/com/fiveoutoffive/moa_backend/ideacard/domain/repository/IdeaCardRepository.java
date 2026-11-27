package com.fiveoutoffive.moa_backend.ideacard.domain.repository;

import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardType;
import com.fiveoutoffive.moa_backend.ideacard.domain.model.IdeaCard;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface IdeaCardRepository {

    IdeaCard save(IdeaCard ideaCard);

    Optional<IdeaCard> findById(Long id);

    /** 삭제되지 않은 카드 행을 잠근다(FOR UPDATE). 같은 카드의 첨부 추가처럼 "세고 나서 넣는" 처리를 차례로 하게 한다. 기다리는 사이 지워졌으면 빈 값이다. */
    Optional<IdeaCard> findActiveByIdForUpdate(Long id);

    List<IdeaCard> findBySessionIdAndDeletedAtIsNullOrderBySortOrderAsc(Long sessionId);

    List<IdeaCard> findBySessionIdAndCardTypeAndDeletedAtIsNullOrderBySortOrderAsc(Long sessionId, CardType cardType);

    long countBySessionIdAndDeletedAtIsNull(Long sessionId);

    long countByRoomIdAndSessionIdIn(Long roomId, Collection<Long> sessionIds);

    List<IdeaCard> findByRoomIdAndDeletedAtIsNull(Long roomId);

    List<IdeaCard> findByRoomIdAndSessionIdInAndDeletedAtIsNull(Long roomId, Collection<Long> sessionIds);

    List<IdeaCard> findByIdIn(Collection<Long> ids);

    long countWritersBySessionId(@Param("sessionId") Long sessionId);

    int findMaxSortOrder(@Param("sessionId") Long sessionId, @Param("cardType") CardType cardType);
}
