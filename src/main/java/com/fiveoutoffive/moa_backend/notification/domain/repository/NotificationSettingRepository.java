package com.fiveoutoffive.moa_backend.notification.domain.repository;

import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationSetting;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface NotificationSettingRepository {

    NotificationSetting save(NotificationSetting notificationSetting);

    Optional<NotificationSetting> findById(Long id);

    List<NotificationSetting> findByAccountIdAndRoomIdIsNull(Long accountId);

    List<NotificationSetting> findByAccountIdAndRoomId(Long accountId, Long roomId);

    void deleteByAccountIdAndRoomId(Long accountId, Long roomId);

    void deleteSetting(@Param("id") Long id);
}
