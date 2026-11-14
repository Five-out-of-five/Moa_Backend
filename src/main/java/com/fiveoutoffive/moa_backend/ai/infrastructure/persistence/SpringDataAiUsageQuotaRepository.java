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

    /**
     * 한도 안이면 1 올리고 1 을 반환한다. 한도에 닿았으면 아무것도 바꾸지 않고 0 을 반환한다.
     * 확인과 증가를 한 문장으로 해서 같은 방의 동시 요청이 한도를 넘지 못하게 한다.
     * 행이 없을 때의 insert 는 limit 을 보지 않으므로 limit 이 0 이하면 호출하지 않는다.
     */
    @Modifying
    @Query(value = "insert into ai_usage_quota (room_id, feature, quota_date, used_count) values (:roomId, :feature, :date, 1) "
            + "on conflict (room_id, feature, quota_date) do update set used_count = ai_usage_quota.used_count + 1 "
            + "where ai_usage_quota.used_count < :limit", nativeQuery = true)
    int tryReserve(@Param("roomId") Long roomId, @Param("feature") String feature, @Param("date") LocalDate date, @Param("limit") int limit);

    @Modifying
    @Query(value = "update ai_usage_quota set used_count = used_count - 1 "
            + "where room_id = :roomId and feature = :feature and quota_date = :date and used_count > 0", nativeQuery = true)
    int release(@Param("roomId") Long roomId, @Param("feature") String feature, @Param("date") LocalDate date);
}
