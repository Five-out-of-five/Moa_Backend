package com.fiveoutoffive.moa_backend.room.domain.repository;

import com.fiveoutoffive.moa_backend.room.domain.model.JoinRequestStatus;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomJoinRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface RoomJoinRequestRepository {

    RoomJoinRequest save(RoomJoinRequest roomJoinRequest);

    Optional<RoomJoinRequest> findById(Long id);

    Optional<RoomJoinRequest> findByRoomIdAndAccountIdAndStatus(Long roomId, Long accountId, JoinRequestStatus status);

    Page<RoomJoinRequest> findByRoomIdAndStatus(Long roomId, JoinRequestStatus status, Pageable pageable);

    List<RoomJoinRequest> findByStatusAndExpiresAtBefore(JoinRequestStatus status, LocalDateTime time);
}
