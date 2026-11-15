package com.fiveoutoffive.moa_backend.ai.domain.repository;

import com.fiveoutoffive.moa_backend.ai.domain.model.AiFeature;
import com.fiveoutoffive.moa_backend.ai.domain.model.AiUsageQuota;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface AiUsageQuotaRepository {

    AiUsageQuota save(AiUsageQuota aiUsageQuota);

    Optional<AiUsageQuota> findById(Long id);

    Optional<AiUsageQuota> findByRoomIdAndFeatureAndQuotaDate(Long roomId, AiFeature feature, LocalDate quotaDate);

    List<AiUsageQuota> findByRoomIdAndQuotaDate(Long roomId, LocalDate quotaDate);

    /** 한도 안이면 사용량을 1 올리고 true. 이미 한도면 false. 결과는 바로 커밋된다. */
    boolean tryReserve(Long roomId, AiFeature feature, LocalDate date, int limit);

    /** 선점한 1회를 돌려준다. 선점한 날짜를 그대로 넘긴다(자정을 넘긴 요청이 다음 날 사용량을 줄이지 않게). */
    void release(Long roomId, AiFeature feature, LocalDate date);
}
