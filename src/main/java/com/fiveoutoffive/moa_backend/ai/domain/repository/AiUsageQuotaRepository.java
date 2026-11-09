package com.fiveoutoffive.moa_backend.ai.domain.repository;

import com.fiveoutoffive.moa_backend.ai.domain.model.AiFeature;
import com.fiveoutoffive.moa_backend.ai.domain.model.AiUsageQuota;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface AiUsageQuotaRepository {

    AiUsageQuota save(AiUsageQuota aiUsageQuota);

    Optional<AiUsageQuota> findById(Long id);

    Optional<AiUsageQuota> findByRoomIdAndFeatureAndQuotaDate(Long roomId, AiFeature feature, LocalDate quotaDate);

    List<AiUsageQuota> findByRoomIdAndQuotaDate(Long roomId, LocalDate quotaDate);

    int increment(@Param("roomId") Long roomId, @Param("feature") String feature, @Param("date") LocalDate date);
}
