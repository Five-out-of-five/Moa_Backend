package com.fiveoutoffive.moa_backend.notification.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationSetting;
import com.fiveoutoffive.moa_backend.notification.domain.repository.NotificationSettingRepository;
import com.fiveoutoffive.moa_backend.notification.infrastructure.mapper.NotificationSettingMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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
}
