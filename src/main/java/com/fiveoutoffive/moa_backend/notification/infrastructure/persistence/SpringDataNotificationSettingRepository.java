package com.fiveoutoffive.moa_backend.notification.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataNotificationSettingRepository extends JpaRepository<NotificationSettingJpaEntity, Long> {

    List<NotificationSettingJpaEntity> findByAccountIdAndRoomIdIsNull(Long accountId);

    List<NotificationSettingJpaEntity> findByAccountIdAndRoomId(Long accountId, Long roomId);

    void deleteByAccountIdAndRoomId(Long accountId, Long roomId);

    @Modifying
    @Query("delete from NotificationSettingJpaEntity s where s.id = :id")
    void deleteSetting(@Param("id") Long id);
}
