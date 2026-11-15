package com.fiveoutoffive.moa_backend.mood.infrastructure.persistence;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataMoodStatusRepository extends JpaRepository<MoodStatusJpaEntity, Long> {

    Optional<MoodStatusJpaEntity> findByRoomIdAndAccountIdAndRecordedDate(Long roomId, Long accountId, LocalDate recordedDate);

    List<MoodStatusJpaEntity> findByRoomIdAndRecordedDate(Long roomId, LocalDate recordedDate);

    List<MoodStatusJpaEntity> findByRoomIdAndRecordedDateBetween(Long roomId, LocalDate from, LocalDate to);

    Optional<MoodStatusJpaEntity> findFirstByRoomIdAndAccountIdOrderByRecordedDateDesc(Long roomId, Long accountId);

    /** 오늘 기분을 한 번에 넣거나 바꾼다. 같은 사람이 동시에 두 번 보내도 유니크 위반 없이 마지막 값이 남는다. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "insert into mood_status (room_id, account_id, mood, comment, recorded_date) values (:roomId, :accountId, :mood, :comment, :date) "
            + "on conflict (room_id, account_id, recorded_date) do update set mood = excluded.mood, comment = excluded.comment, updated_at = :now", nativeQuery = true)
    int upsert(@Param("roomId") Long roomId, @Param("accountId") Long accountId, @Param("mood") String mood, @Param("comment") String comment,
               @Param("date") LocalDate date, @Param("now") java.time.LocalDateTime now);
}
