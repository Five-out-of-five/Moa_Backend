package com.fiveoutoffive.moa_backend.room.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.room.domain.model.RoomStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 방 (프로젝트 조직 단위 워크스페이스) (room). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "image_file_id")
    private Long imageFileId;

    @Column(name = "invite_code", nullable = false, length = 32)
    private String inviteCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RoomStatus status;

    @Column(name = "inactive_reason", length = 500)
    private String inactiveReason;

    @Column(name = "inactivated_at")
    private LocalDateTime inactivatedAt;

    @Column(name = "inactivated_by_account_id")
    private Long inactivatedByAccountId;

    @Column(name = "created_by_account_id", nullable = false)
    private Long createdByAccountId;

    @Column(name = "layout_version", nullable = false)
    private Long layoutVersion;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public RoomJpaEntity(
            Long id,
            String name,
            String description,
            Long imageFileId,
            String inviteCode,
            RoomStatus status,
            String inactiveReason,
            LocalDateTime inactivatedAt,
            Long inactivatedByAccountId,
            Long createdByAccountId,
            Long layoutVersion,
            LocalDateTime lastActivityAt,
            LocalDateTime deletedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageFileId = imageFileId;
        this.inviteCode = inviteCode;
        this.status = status;
        this.inactiveReason = inactiveReason;
        this.inactivatedAt = inactivatedAt;
        this.inactivatedByAccountId = inactivatedByAccountId;
        this.createdByAccountId = createdByAccountId;
        this.layoutVersion = layoutVersion;
        this.lastActivityAt = lastActivityAt;
        this.deletedAt = deletedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // DB 기본값과 같은 값을 채운다. JPA 는 null 을 그대로 INSERT 해서 DB 기본값이 쓰이지 않기 때문이다.
    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (this.status == null) {
            this.status = RoomStatus.ACTIVE;
        }
        if (this.layoutVersion == null) {
            this.layoutVersion = 0L;
        }
        if (this.createdAt == null) {
            this.createdAt = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
