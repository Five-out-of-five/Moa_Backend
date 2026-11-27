package com.fiveoutoffive.moa_backend.room.domain.repository;

import com.fiveoutoffive.moa_backend.room.domain.model.RoomMember;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomMemberStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface RoomMemberRepository {

    RoomMember save(RoomMember roomMember);

    Optional<RoomMember> findById(Long id);

    Optional<RoomMember> findByRoomIdAndAccountId(Long roomId, Long accountId);

    List<RoomMember> findByAccountIdAndStatus(Long accountId, RoomMemberStatus status);

    List<RoomMember> findByRoomIdAndStatus(Long roomId, RoomMemberStatus status);

    List<RoomMember> findByRoomIdAndAccountIdIn(Long roomId, Collection<Long> accountIds);

    long countByRoomIdAndStatus(Long roomId, RoomMemberStatus status);

    boolean existsByRoomIdAndNicknameAndStatus(Long roomId, String nickname, RoomMemberStatus status);

    /** 유니크 인덱스 때문에 순서가 중요한 변경(방장 위임)에 쓴다. */
    RoomMember saveAndFlush(RoomMember roomMember);

    /**
     * 활동 중인 멤버의 마지막 브리핑 확인 시각만 바꾼다. 행 전체를 다시 저장하지 않으므로 같은 순간의 강퇴·나가기·방장 위임을 덮어쓰지 않는다.
     * 바꾼 행이 있으면 true(그사이 나갔거나 강퇴됐으면 false).
     */
    boolean updateLastBriefingCheckedAt(Long roomId, Long accountId, LocalDateTime checkedAt);
}
