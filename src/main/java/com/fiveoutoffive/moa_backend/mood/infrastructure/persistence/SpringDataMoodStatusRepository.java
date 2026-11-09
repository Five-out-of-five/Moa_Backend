package com.fiveoutoffive.moa_backend.mood.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataMoodStatusRepository extends JpaRepository<MoodStatusJpaEntity, Long> {

    Optional<MoodStatusJpaEntity> findByRoomIdAndAccountIdAndRecordedDate(Long roomId, Long accountId, LocalDate recordedDate);

    List<MoodStatusJpaEntity> findByRoomIdAndRecordedDate(Long roomId, LocalDate recordedDate);

    List<MoodStatusJpaEntity> findByRoomIdAndRecordedDateBetween(Long roomId, LocalDate from, LocalDate to);

    Optional<MoodStatusJpaEntity> findFirstByRoomIdAndAccountIdOrderByRecordedDateDesc(Long roomId, Long accountId);
}
