package com.fiveoutoffive.moa_backend.notification.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationSetting;
import com.fiveoutoffive.moa_backend.notification.domain.repository.NotificationSettingRepository;
import com.fiveoutoffive.moa_backend.notification.infrastructure.mapper.NotificationSettingMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationSettingRepositoryAdapter implements NotificationSettingRepository {

    private final SpringDataNotificationSettingRepository springDataRepository;
    private final NotificationSettingMapper notificationSettingMapper;

    @Override
    public NotificationSetting save(NotificationSetting notificationSetting) {
        NotificationSettingJpaEntity saved = springDataRepository.save(notificationSettingMapper.toJpaEntity(notificationSetting));
        return notificationSettingMapper.toDomain(saved);
    }

    @Override
    public Optional<NotificationSetting> findById(Long id) {
        return springDataRepository.findById(id)
                .map(notificationSettingMapper::toDomain);
    }

    @Override
    public List<NotificationSetting> findByAccountIdAndRoomIdIsNull(Long accountId) {
        return springDataRepository.findByAccountIdAndRoomIdIsNull(accountId).stream().map(notificationSettingMapper::toDomain).toList();
    }

    @Override
    public List<NotificationSetting> findByAccountIdAndRoomId(Long accountId, Long roomId) {
        return springDataRepository.findByAccountIdAndRoomId(accountId, roomId).stream().map(notificationSettingMapper::toDomain).toList();
    }

    @Override
    public void deleteByAccountIdAndRoomId(Long accountId, Long roomId) {
        springDataRepository.deleteByAccountIdAndRoomId(accountId, roomId);
    }

    @Override
    public void deleteSetting(@Param("id") Long id) {
        springDataRepository.deleteSetting(id);
    }
}
