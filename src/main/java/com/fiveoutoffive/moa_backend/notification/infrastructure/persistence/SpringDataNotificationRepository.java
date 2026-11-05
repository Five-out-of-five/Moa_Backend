package com.fiveoutoffive.moa_backend.notification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataNotificationRepository extends JpaRepository<NotificationJpaEntity, Long> {
}
