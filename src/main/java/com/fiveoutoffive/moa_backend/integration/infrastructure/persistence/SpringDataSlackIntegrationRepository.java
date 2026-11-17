package com.fiveoutoffive.moa_backend.integration.infrastructure.persistence;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSlackIntegrationRepository extends JpaRepository<SlackIntegrationJpaEntity, Long> {

    Optional<SlackIntegrationJpaEntity> findByRoomId(Long roomId);

    void deleteById(Long id);

    /**
     * 발송 결과만 반영한다(실패 횟수, 연속 실패 시 비활성화). 다른 설정 칸은 건드리지 않는다.
     * 발송 중에 방장이 주소·이벤트를 바꿔도 덮어쓰지 않고, 연동이 지워졌으면 0행이라 되살아나지 않는다.
     */
    @Modifying
    @Query(value = "update slack_integration set "
            + "consecutive_fail_count = case when :ok then 0 else consecutive_fail_count + 1 end, "
            + "enabled = case when not :ok and consecutive_fail_count + 1 >= :threshold then false else enabled end, "
            + "disabled_at = case when not :ok and consecutive_fail_count + 1 >= :threshold and enabled then :now else disabled_at end "
            + "where id = :id", nativeQuery = true)
    int recordDelivery(@Param("id") Long id, @Param("ok") boolean ok, @Param("threshold") int threshold, @Param("now") java.time.LocalDateTime now);
}
