package com.fiveoutoffive.moa_backend.room.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.room.domain.model.RoomStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataRoomRepository extends JpaRepository<RoomJpaEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RoomJpaEntity r where r.id = :id")
    Optional<RoomJpaEntity> findLockedById(@Param("id") Long id);

    Optional<RoomJpaEntity> findByInviteCodeAndDeletedAtIsNull(String inviteCode);

    long countByCreatedByAccountIdAndDeletedAtIsNull(Long accountId);

    List<RoomJpaEntity> findByIdInAndDeletedAtIsNull(Collection<Long> ids);

    List<RoomJpaEntity> findByDeletedAtBefore(LocalDateTime time);

    void deleteById(Long id);

    long countByDeletedAtIsNull();

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime from, LocalDateTime to);

    @Query("select r from RoomJpaEntity r left join AccountJpaEntity a on a.id = r.createdByAccountId where r.deletedAt is null and (cast(:keyword as string) is null or lower(r.name) like lower(concat('%', cast(:keyword as string), '%')) or lower(a.name) like lower(concat('%', cast(:keyword as string), '%'))) and (:status is null or r.status = :status) and (cast(:inactiveBefore as timestamp) is null or r.lastActivityAt is null or r.lastActivityAt < :inactiveBefore)")
    Page<RoomJpaEntity> search(@Param("keyword") String keyword, @Param("status") RoomStatus status, @Param("inactiveBefore") LocalDateTime inactiveBefore, Pageable pageable);

    /** 관리자 방 목록 sort=REPORT_COUNT. 조건은 search 와 같고, 신고 수가 많은 순(같으면 최근 개설 순)으로 정렬한다. */
    @Query(value = "select r from RoomJpaEntity r left join AccountJpaEntity a on a.id = r.createdByAccountId left join ReportJpaEntity rp on rp.roomId = r.id where r.deletedAt is null and (cast(:keyword as string) is null or lower(r.name) like lower(concat('%', cast(:keyword as string), '%')) or lower(a.name) like lower(concat('%', cast(:keyword as string), '%'))) and (:status is null or r.status = :status) and (cast(:inactiveBefore as timestamp) is null or r.lastActivityAt is null or r.lastActivityAt < :inactiveBefore) group by r order by count(rp) desc, r.createdAt desc",
            countQuery = "select count(r) from RoomJpaEntity r left join AccountJpaEntity a on a.id = r.createdByAccountId where r.deletedAt is null and (cast(:keyword as string) is null or lower(r.name) like lower(concat('%', cast(:keyword as string), '%')) or lower(a.name) like lower(concat('%', cast(:keyword as string), '%'))) and (:status is null or r.status = :status) and (cast(:inactiveBefore as timestamp) is null or r.lastActivityAt is null or r.lastActivityAt < :inactiveBefore)")
    Page<RoomJpaEntity> searchOrderByReportCount(@Param("keyword") String keyword, @Param("status") RoomStatus status, @Param("inactiveBefore") LocalDateTime inactiveBefore, Pageable pageable);
}
