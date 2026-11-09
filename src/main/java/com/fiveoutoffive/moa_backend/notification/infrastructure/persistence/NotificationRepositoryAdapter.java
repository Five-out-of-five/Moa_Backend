package com.fiveoutoffive.moa_backend.notification.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.notification.domain.model.Notification;
import com.fiveoutoffive.moa_backend.notification.domain.repository.NotificationRepository;
import com.fiveoutoffive.moa_backend.notification.infrastructure.mapper.NotificationMapper;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
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

    @Override
    public Page<Notification> findByOwnerAccountId(Long ownerAccountId, Pageable pageable) {
        return springDataRepository.findByOwnerAccountId(ownerAccountId, pageable).map(notificationMapper::toDomain);
    }

    @Override
    public Page<Notification> findByOwnerAccountIdAndRead(Long ownerAccountId, Boolean read, Pageable pageable) {
        return springDataRepository.findByOwnerAccountIdAndRead(ownerAccountId, read, pageable).map(notificationMapper::toDomain);
    }

    @Override
    public long countByOwnerAccountIdAndRead(Long ownerAccountId, Boolean read) {
        return springDataRepository.countByOwnerAccountIdAndRead(ownerAccountId, read);
    }

    @Override
    public void deleteById(Long id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public int markAllRead(@Param("owner") Long owner, @Param("now") LocalDateTime now) {
        return springDataRepository.markAllRead(owner, now);
    }

    @Override
    public int deleteOlderThan(@Param("before") LocalDateTime before) {
        return springDataRepository.deleteOlderThan(before);
    }
}
