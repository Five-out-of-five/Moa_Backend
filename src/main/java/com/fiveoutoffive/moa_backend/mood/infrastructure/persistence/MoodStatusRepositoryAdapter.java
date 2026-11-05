package com.fiveoutoffive.moa_backend.mood.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.mood.domain.model.MoodStatus;
import com.fiveoutoffive.moa_backend.mood.domain.repository.MoodStatusRepository;
import com.fiveoutoffive.moa_backend.mood.infrastructure.mapper.MoodStatusMapper;
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
}
