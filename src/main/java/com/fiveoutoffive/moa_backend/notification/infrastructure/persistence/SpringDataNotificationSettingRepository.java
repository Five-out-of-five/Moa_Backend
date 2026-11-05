package com.fiveoutoffive.moa_backend.notification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataNotificationSettingRepository extends JpaRepository<NotificationSettingJpaEntity, Long> {
}
