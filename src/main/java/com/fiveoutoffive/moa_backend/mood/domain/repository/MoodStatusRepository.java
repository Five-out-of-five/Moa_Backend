package com.fiveoutoffive.moa_backend.mood.domain.repository;

import com.fiveoutoffive.moa_backend.mood.domain.model.MoodStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface MoodStatusRepository {

    MoodStatus save(MoodStatus moodStatus);

    Optional<MoodStatus> findById(Long id);

    Optional<MoodStatus> findByRoomIdAndAccountIdAndRecordedDate(Long roomId, Long accountId, LocalDate recordedDate);

    List<MoodStatus> findByRoomIdAndRecordedDate(Long roomId, LocalDate recordedDate);

    List<MoodStatus> findByRoomIdAndRecordedDateBetween(Long roomId, LocalDate from, LocalDate to);

    Optional<MoodStatus> findFirstByRoomIdAndAccountIdOrderByRecordedDateDesc(Long roomId, Long accountId);
}
