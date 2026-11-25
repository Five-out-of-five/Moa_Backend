package com.fiveoutoffive.moa_backend.integration.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.integration.domain.model.NotionExportMode;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataNotionIntegrationRepository extends JpaRepository<NotionIntegrationJpaEntity, Long> {

    Optional<NotionIntegrationJpaEntity> findByRoomId(Long roomId);

    void deleteById(Long id);

    /**
     * 내보내기 설정만 반영한다. 저장 전에 노션에서 이름을 조회하는 동안 방장이 다시 연결하거나 해제할 수 있다.
     * 행 전체를 저장하면 새 토큰을 옛 값으로 덮어쓰므로 설정 칸만 바꾼다. 연동이 지워졌으면 0행이다.
     */
    @Modifying(clearAutomatically = true)
    @Query("update NotionIntegrationJpaEntity n set n.targetDatabaseId = :databaseId, n.targetDatabaseTitle = :databaseTitle, "
            + "n.exportMode = :exportMode, n.updatedAt = :now where n.id = :id")
    int updateExportSettings(@Param("id") Long id, @Param("databaseId") String databaseId, @Param("databaseTitle") String databaseTitle,
            @Param("exportMode") NotionExportMode exportMode, @Param("now") LocalDateTime now);

    /**
     * 연동별 내보내기 잠금(트랜잭션 끝에 풀림). 같은 방 내보내기(버튼 연타, 수동·자동 겹침)가 차례로 처리된다.
     * 행 잠금(for update)이 아니라 advisory 잠금을 쓴다. 내보내기 실패 시 토큰 만료 표시는 별도 트랜잭션에서 같은 행을 고치는데,
     * 행을 잠그고 있으면 그 갱신이 영원히 기다린다(자기 교착).
     */
    @Query(value = "select 1 from (select pg_advisory_xact_lock(1001, cast(:id as integer))) t", nativeQuery = true)
    Integer lockExport(@Param("id") Long integrationId);
}
