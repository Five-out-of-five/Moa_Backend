package com.fiveoutoffive.moa_backend.notification.infrastructure.persistence;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataNotificationRepository extends JpaRepository<NotificationJpaEntity, Long> {

    Page<NotificationJpaEntity> findByOwnerAccountId(Long ownerAccountId, Pageable pageable);

    Page<NotificationJpaEntity> findByOwnerAccountIdAndRead(Long ownerAccountId, Boolean read, Pageable pageable);

    long countByOwnerAccountIdAndRead(Long ownerAccountId, Boolean read);

    void deleteById(Long id);

    @Modifying
    @Query("update NotificationJpaEntity n set n.read = true, n.readAt = :now where n.ownerAccountId = :owner and n.read = false")
    int markAllRead(@Param("owner") Long owner, @Param("now") LocalDateTime now);

    @Modifying
    @Query("delete from NotificationJpaEntity n where n.ownerAccountId = :owner and n.roomId = :roomId"
            + " and n.notificationGroup <> com.fiveoutoffive.moa_backend.notification.domain.model.NotificationGroup.SYSTEM"
            + " and n.notificationType not in (com.fiveoutoffive.moa_backend.notification.domain.model.NotificationType.MEMBER_KICKED,"
            + " com.fiveoutoffive.moa_backend.notification.domain.model.NotificationType.JOIN_PROCESSED)")
    int deleteLeftRoomNotifications(@Param("owner") Long owner, @Param("roomId") Long roomId);

    @Modifying
    @Query("delete from NotificationJpaEntity n where n.createdAt < :before")
    int deleteOlderThan(@Param("before") LocalDateTime before);
}
