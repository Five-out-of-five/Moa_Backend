package com.fiveoutoffive.moa_backend.account.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.account.domain.model.AccountRole;
import com.fiveoutoffive.moa_backend.account.domain.model.AccountStatus;
import com.fiveoutoffive.moa_backend.account.domain.model.SocialProvider;
import com.fiveoutoffive.moa_backend.account.domain.model.ToneMode;
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

/** 회원 계정 (소셜 로그인 전용, 소셜 연결·약관 동의 포함) (account). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "account")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private SocialProvider provider;

    @Column(name = "provider_uid", length = 255)
    private String providerUid;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "name", length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private AccountRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccountStatus status;

    @Column(name = "suspended_until")
    private LocalDateTime suspendedUntil;

    @Enumerated(EnumType.STRING)
    @Column(name = "tone_mode", nullable = false, length = 20)
    private ToneMode toneMode;

    @Column(name = "last_room_id")
    private Long lastRoomId;

    @Column(name = "report_blocked_until")
    private LocalDateTime reportBlockedUntil;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "terms_agreed_at", nullable = false)
    private LocalDateTime termsAgreedAt;

    @Column(name = "privacy_agreed_at", nullable = false)
    private LocalDateTime privacyAgreedAt;

    @Column(name = "purge_at")
    private LocalDateTime purgeAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public AccountJpaEntity(
            Long id,
            SocialProvider provider,
            String providerUid,
            String email,
            String name,
            AccountRole role,
            AccountStatus status,
            LocalDateTime suspendedUntil,
            ToneMode toneMode,
            Long lastRoomId,
            LocalDateTime reportBlockedUntil,
            LocalDateTime lastLoginAt,
            LocalDateTime termsAgreedAt,
            LocalDateTime privacyAgreedAt,
            LocalDateTime purgeAt,
            LocalDateTime deletedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.provider = provider;
        this.providerUid = providerUid;
        this.email = email;
        this.name = name;
        this.role = role;
        this.status = status;
        this.suspendedUntil = suspendedUntil;
        this.toneMode = toneMode;
        this.lastRoomId = lastRoomId;
        this.reportBlockedUntil = reportBlockedUntil;
        this.lastLoginAt = lastLoginAt;
        this.termsAgreedAt = termsAgreedAt;
        this.privacyAgreedAt = privacyAgreedAt;
        this.purgeAt = purgeAt;
        this.deletedAt = deletedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // DB 기본값과 같은 값을 채운다. JPA 는 null 을 그대로 INSERT 해서 DB 기본값이 쓰이지 않기 때문이다.
    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (this.role == null) {
            this.role = AccountRole.USER;
        }
        if (this.status == null) {
            this.status = AccountStatus.ACTIVE;
        }
        if (this.toneMode == null) {
            this.toneMode = ToneMode.POLISH;
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
