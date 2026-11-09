package com.fiveoutoffive.moa_backend.mood.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.mood.domain.model.MoodStatus;
import com.fiveoutoffive.moa_backend.mood.domain.repository.MoodStatusRepository;
import com.fiveoutoffive.moa_backend.mood.infrastructure.mapper.MoodStatusMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MoodStatusRepositoryAdapter implements MoodStatusRepository {

    private final SpringDataMoodStatusRepository springDataRepository;
    private final MoodStatusMapper moodStatusMapper;

    @Override
    public MoodStatus save(MoodStatus moodStatus) {
        MoodStatusJpaEntity saved = springDataRepository.save(moodStatusMapper.toJpaEntity(moodStatus));
        return moodStatusMapper.toDomain(saved);
    }

    @Override
    public Optional<MoodStatus> findById(Long id) {
        return springDataRepository.findById(id)
                .map(moodStatusMapper::toDomain);
    }

    @Override
    public Optional<MoodStatus> findByRoomIdAndAccountIdAndRecordedDate(Long roomId, Long accountId, LocalDate recordedDate) {
        return springDataRepository.findByRoomIdAndAccountIdAndRecordedDate(roomId, accountId, recordedDate).map(moodStatusMapper::toDomain);
    }

    @Override
    public List<MoodStatus> findByRoomIdAndRecordedDate(Long roomId, LocalDate recordedDate) {
        return springDataRepository.findByRoomIdAndRecordedDate(roomId, recordedDate).stream().map(moodStatusMapper::toDomain).toList();
    }

    @Override
    public List<MoodStatus> findByRoomIdAndRecordedDateBetween(Long roomId, LocalDate from, LocalDate to) {
        return springDataRepository.findByRoomIdAndRecordedDateBetween(roomId, from, to).stream().map(moodStatusMapper::toDomain).toList();
    }

    @Override
    public Optional<MoodStatus> findFirstByRoomIdAndAccountIdOrderByRecordedDateDesc(Long roomId, Long accountId) {
        return springDataRepository.findFirstByRoomIdAndAccountIdOrderByRecordedDateDesc(roomId, accountId).map(moodStatusMapper::toDomain);
    }
}
