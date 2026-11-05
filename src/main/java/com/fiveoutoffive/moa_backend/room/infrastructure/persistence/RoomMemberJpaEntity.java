package com.fiveoutoffive.moa_backend.room.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.room.domain.model.RoomMemberRole;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomMemberStatus;
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

/** 방 멤버 + 방별 프로필 (room_member). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "room_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomMemberJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private RoomMemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RoomMemberStatus status;

    @Column(name = "nickname", length = 20)
    private String nickname;

    @Column(name = "profile_file_id")
    private Long profileFileId;

    @Column(name = "position_label", length = 20)
    private String positionLabel;

    @Column(name = "notification_muted", nullable = false)
    private Boolean notificationMuted;

    @Column(name = "last_briefing_checked_at")
    private LocalDateTime lastBriefingCheckedAt;

    @Column(name = "profile_setup_at")
    private LocalDateTime profileSetupAt;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Column(name = "kicked_at")
    private LocalDateTime kickedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public RoomMemberJpaEntity(
            Long id,
            Long roomId,
            Long accountId,
            RoomMemberRole role,
            RoomMemberStatus status,
            String nickname,
            Long profileFileId,
            String positionLabel,
            Boolean notificationMuted,
            LocalDateTime lastBriefingCheckedAt,
            LocalDateTime profileSetupAt,
            LocalDateTime joinedAt,
            LocalDateTime leftAt,
            LocalDateTime kickedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.roomId = roomId;
        this.accountId = accountId;
        this.role = role;
        this.status = status;
        this.nickname = nickname;
        this.profileFileId = profileFileId;
        this.positionLabel = positionLabel;
        this.notificationMuted = notificationMuted;
        this.lastBriefingCheckedAt = lastBriefingCheckedAt;
        this.profileSetupAt = profileSetupAt;
        this.joinedAt = joinedAt;
        this.leftAt = leftAt;
        this.kickedAt = kickedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // DB 기본값과 같은 값을 채운다. JPA 는 null 을 그대로 INSERT 해서 DB 기본값이 쓰이지 않기 때문이다.
    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (this.role == null) {
            this.role = RoomMemberRole.MEMBER;
        }
        if (this.status == null) {
            this.status = RoomMemberStatus.ACTIVE;
        }
        if (this.notificationMuted == null) {
            this.notificationMuted = Boolean.FALSE;
        }
        if (this.joinedAt == null) {
            this.joinedAt = now;
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
