package com.fiveoutoffive.moa_backend.ai.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.ai.domain.model.AiFeature;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataAiUsageQuotaRepository extends JpaRepository<AiUsageQuotaJpaEntity, Long> {

    Optional<AiUsageQuotaJpaEntity> findByRoomIdAndFeatureAndQuotaDate(Long roomId, AiFeature feature, LocalDate quotaDate);

    List<AiUsageQuotaJpaEntity> findByRoomIdAndQuotaDate(Long roomId, LocalDate quotaDate);

    @Modifying
    @Query(value = "insert into ai_usage_quota (room_id, feature, quota_date, used_count) values (:roomId, :feature, :date, 1) on conflict (room_id, feature, quota_date) do update set used_count = ai_usage_quota.used_count + 1", nativeQuery = true)
    int increment(@Param("roomId") Long roomId, @Param("feature") String feature, @Param("date") LocalDate date);
}
