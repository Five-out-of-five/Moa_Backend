package com.fiveoutoffive.moa_backend.notification.domain.repository;

import com.fiveoutoffive.moa_backend.notification.domain.model.Notification;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findById(Long id);

    Page<Notification> findByOwnerAccountId(Long ownerAccountId, Pageable pageable);

    Page<Notification> findByOwnerAccountIdAndRead(Long ownerAccountId, Boolean read, Pageable pageable);

    long countByOwnerAccountIdAndRead(Long ownerAccountId, Boolean read);

    void deleteById(Long id);

    int markAllRead(@Param("owner") Long owner, @Param("now") LocalDateTime now);

    /** 방을 나간 회원의 그 방 알림을 지운다. 관리자·시스템 알림과 퇴출·참여 결과 알림은 남긴다. */
    int deleteLeftRoomNotifications(Long ownerAccountId, Long roomId);

    int deleteOlderThan(@Param("before") LocalDateTime before);
}
