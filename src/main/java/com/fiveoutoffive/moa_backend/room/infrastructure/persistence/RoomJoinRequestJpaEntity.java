package com.fiveoutoffive.moa_backend.room.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.room.domain.model.JoinRequestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

/** 방 참여 요청 ('승인 필요' 방식) (room_join_request). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "room_join_request")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomJoinRequestJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private JoinRequestStatus status;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "processed_by_account_id")
    private Long processedByAccountId;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public RoomJoinRequestJpaEntity(
            Long id,
            Long roomId,
            Long accountId,
            JoinRequestStatus status,
            LocalDateTime requestedAt,
            LocalDateTime expiresAt,
            LocalDateTime processedAt,
            Long processedByAccountId
    ) {
        this.id = id;
        this.roomId = roomId;
        this.accountId = accountId;
        this.status = status;
        this.requestedAt = requestedAt;
        this.expiresAt = expiresAt;
        this.processedAt = processedAt;
        this.processedByAccountId = processedByAccountId;
    }

    // DB 기본값과 같은 값을 채운다. JPA 는 null 을 그대로 INSERT 해서 DB 기본값이 쓰이지 않기 때문이다.
    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (this.status == null) {
            this.status = JoinRequestStatus.PENDING;
        }
        if (this.requestedAt == null) {
            this.requestedAt = now;
        }
    }
}
