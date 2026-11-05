package com.fiveoutoffive.moa_backend.notification.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.notification.domain.model.Notification;
import com.fiveoutoffive.moa_backend.notification.domain.repository.NotificationRepository;
import com.fiveoutoffive.moa_backend.notification.infrastructure.mapper.NotificationMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepository {

    private final SpringDataNotificationRepository springDataRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public Notification save(Notification notification) {
        NotificationJpaEntity saved = springDataRepository.save(notificationMapper.toJpaEntity(notification));
        return notificationMapper.toDomain(saved);
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return springDataRepository.findById(id)
                .map(notificationMapper::toDomain);
    }
}
