package com.fiveoutoffive.moa_backend.ideacard.domain.repository;

import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardType;
import com.fiveoutoffive.moa_backend.ideacard.domain.model.IdeaCard;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.query.Param;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface IdeaCardRepository {

    IdeaCard save(IdeaCard ideaCard);

    Optional<IdeaCard> findById(Long id);

    /** 삭제되지 않은 카드 행을 잠근다(FOR UPDATE). 같은 카드의 첨부 추가처럼 "세고 나서 넣는" 처리를 차례로 하게 한다. 기다리는 사이 지워졌으면 빈 값이다. */
    Optional<IdeaCard> findActiveByIdForUpdate(Long id);

    /**
     * 같은 작성자·같은 재시도 키의 카드 등록을 트랜잭션이 끝날 때까지 한 번에 하나씩만 하게 한다(PostgreSQL advisory lock).
     * 앞 요청이 저장하는 중이면 기다렸다가, 끝난 뒤 findByWriterAccountIdAndRequestId 로 그 카드를 찾게 된다.
     */
    void lockCreateRequest(Long writerAccountId, UUID requestId);

    /** 재시도 키로 만든 카드. 삭제된 카드도 돌려준다(되살리지 않고 404 로 알리기 위해). */
    Optional<IdeaCard> findByWriterAccountIdAndRequestId(Long writerAccountId, UUID requestId);

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
