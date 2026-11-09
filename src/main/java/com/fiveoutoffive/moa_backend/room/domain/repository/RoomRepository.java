package com.fiveoutoffive.moa_backend.room.domain.repository;

import com.fiveoutoffive.moa_backend.room.domain.model.Room;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface RoomRepository {

    Room save(Room room);

    Optional<Room> findById(Long id);

    Optional<Room> findLockedById(@Param("id") Long id);

    Optional<Room> findByInviteCodeAndDeletedAtIsNull(String inviteCode);

    long countByCreatedByAccountIdAndDeletedAtIsNull(Long accountId);

    List<Room> findByIdInAndDeletedAtIsNull(Collection<Long> ids);

    List<Room> findByDeletedAtBefore(LocalDateTime time);

    void deleteById(Long id);

    long countByDeletedAtIsNull();

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime from, LocalDateTime to);

    Page<Room> search(@Param("keyword") String keyword, @Param("status") RoomStatus status, @Param("inactiveBefore") LocalDateTime inactiveBefore, Pageable pageable);
}
