package com.fiveoutoffive.moa_backend.dashboard.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** AI 구성 적용 직전 배치 (되돌리기 1회용) (room_layout_snapshot). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "room_layout_snapshot")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomLayoutSnapshotJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "layout_json", nullable = false)
    private String layoutJson;

    @Column(name = "created_by_account_id")
    private Long createdByAccountId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public RoomLayoutSnapshotJpaEntity(
            Long id,
            Long roomId,
            String layoutJson,
            Long createdByAccountId,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.roomId = roomId;
        this.layoutJson = layoutJson;
        this.createdByAccountId = createdByAccountId;
        this.createdAt = createdAt;
    }

    // DB 기본값과 같은 값을 채운다. JPA 는 null 을 그대로 INSERT 해서 DB 기본값이 쓰이지 않기 때문이다.
    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (this.createdAt == null) {
            this.createdAt = now;
        }
    }
}
