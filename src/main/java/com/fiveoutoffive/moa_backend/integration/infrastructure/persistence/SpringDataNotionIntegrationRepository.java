package com.fiveoutoffive.moa_backend.integration.infrastructure.persistence;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataNotionIntegrationRepository extends JpaRepository<NotionIntegrationJpaEntity, Long> {

    Optional<NotionIntegrationJpaEntity> findByRoomId(Long roomId);

    void deleteById(Long id);

    /**
     * 연동별 내보내기 잠금(트랜잭션 끝에 풀림). 같은 방 내보내기(버튼 연타, 수동·자동 겹침)가 차례로 처리된다.
     * 행 잠금(for update)이 아니라 advisory 잠금을 쓴다. 내보내기 실패 시 토큰 만료 표시는 별도 트랜잭션에서 같은 행을 고치는데,
     * 행을 잠그고 있으면 그 갱신이 영원히 기다린다(자기 교착).
     */
    @Query(value = "select 1 from (select pg_advisory_xact_lock(1001, cast(:id as integer))) t", nativeQuery = true)
    Integer lockExport(@Param("id") Long integrationId);
}
