package com.fiveoutoffive.moa_backend.room.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.room.domain.model.RoomMemberStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataRoomMemberRepository extends JpaRepository<RoomMemberJpaEntity, Long> {

    Optional<RoomMemberJpaEntity> findByRoomIdAndAccountId(Long roomId, Long accountId);

    List<RoomMemberJpaEntity> findByAccountIdAndStatus(Long accountId, RoomMemberStatus status);

    List<RoomMemberJpaEntity> findByRoomIdAndStatus(Long roomId, RoomMemberStatus status);

    List<RoomMemberJpaEntity> findByRoomIdAndAccountIdIn(Long roomId, Collection<Long> accountIds);

    long countByRoomIdAndStatus(Long roomId, RoomMemberStatus status);

    boolean existsByRoomIdAndNicknameAndStatus(Long roomId, String nickname, RoomMemberStatus status);

    /** 마지막 브리핑 확인 시각 한 칸만 바꾼다. 같은 순간 강퇴·나가기가 커밋되면 상태 조건에서 걸러져 0 이다. */
    @Modifying(flushAutomatically = true)
    @Query("update RoomMemberJpaEntity m set m.lastBriefingCheckedAt = :checkedAt where m.roomId = :roomId and m.accountId = :accountId and m.status = :active")
    int updateLastBriefingCheckedAt(@Param("roomId") Long roomId, @Param("accountId") Long accountId, @Param("checkedAt") LocalDateTime checkedAt,
                                    @Param("active") RoomMemberStatus active);
}
