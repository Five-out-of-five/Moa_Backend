package com.fiveoutoffive.moa_backend.briefing.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.briefing.domain.model.BoardEventType;
import com.fiveoutoffive.moa_backend.briefing.domain.model.BriefingDismissal;
import com.fiveoutoffive.moa_backend.briefing.domain.repository.BriefingDismissalRepository;
import com.fiveoutoffive.moa_backend.briefing.infrastructure.mapper.BriefingDismissalMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BriefingDismissalRepositoryAdapter implements BriefingDismissalRepository {

    private final SpringDataBriefingDismissalRepository springDataRepository;
    private final BriefingDismissalMapper briefingDismissalMapper;

    @Override
    public BriefingDismissal save(BriefingDismissal briefingDismissal) {
        BriefingDismissalJpaEntity saved = springDataRepository.save(briefingDismissalMapper.toJpaEntity(briefingDismissal));
        return briefingDismissalMapper.toDomain(saved);
    }

    @Override
    public Optional<BriefingDismissal> findById(Long id) {
        return springDataRepository.findById(id)
                .map(briefingDismissalMapper::toDomain);
    }

    @Override
    public List<BriefingDismissal> findByRoomIdAndAccountId(Long roomId, Long accountId) {
        return springDataRepository.findByRoomIdAndAccountId(roomId, accountId).stream().map(briefingDismissalMapper::toDomain).toList();
    }

    @Override
    public void deleteByRoomIdAndAccountId(Long roomId, Long accountId) {
        springDataRepository.deleteByRoomIdAndAccountId(roomId, accountId);
    }

    @Override
    public Optional<BriefingDismissal> findByRoomIdAndAccountIdAndEventType(Long roomId, Long accountId, BoardEventType eventType) {
        return springDataRepository.findByRoomIdAndAccountIdAndEventType(roomId, accountId, eventType).map(briefingDismissalMapper::toDomain);
    }
}
