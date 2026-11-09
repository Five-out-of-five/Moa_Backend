package com.fiveoutoffive.moa_backend.account.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원 계정 (소셜 로그인 전용, 소셜 연결·약관 동의 포함) (account)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    /** PK */
    private Long id;

    /** 가입한 소셜 제공자 (계정당 1개, 변경·해제 불가. GITHUB 은 추후 확장) (NOT NULL) */
    private SocialProvider provider;

    /** 제공자 측 사용자 식별자 (로그인 시 조회 키). 파기 시 NULL */
    private String providerUid;

    /** 소셜 제공자 이메일. 수정 불가. 파기 시 NULL */
    private String email;

    /** 기본 프로필 이름 (기본 프로필에는 이미지 없음, 이미지는 방별로만). 가입 시 소셜 제공자 이름 자동 입력, 계정 설정에서 수정. 파기 시 NULL */
    private String name;

    /** 서비스 권한 (방 내 권한과 별개) (NOT NULL) */
    private AccountRole role;

    /** 회원 상태 (NOT NULL) */
    private AccountStatus status;

    /** 정지 종료 시각. status=SUSPENDED 이고 NULL 이면 영구 정지 */
    private LocalDateTime suspendedUntil;

    /** 톤앤매너 순화 강도 (계정 설정 > AI 설정) (NOT NULL) */
    private ToneMode toneMode;

    /** 마지막으로 머문 방 (로그인 시 복원) */
    private Long lastRoomId;

    /** 허위 신고 반복 시 신고 기능 제한 종료 시각 */
    private LocalDateTime reportBlockedUntil;

    /** 마지막 로그인 시각 */
    private LocalDateTime lastLoginAt;

    /** 서비스 이용약관 동의 시각 (필수, 동의 없이는 가입 불가) (NOT NULL) */
    private LocalDateTime termsAgreedAt;

    /** 개인정보 처리방침 동의 시각 (필수, 동의 없이는 가입 불가) (NOT NULL) */
    private LocalDateTime privacyAgreedAt;

    /** 개인정보 파기 예정 시각 (탈퇴 + 30일). 이 시각 이후 재가입 가능 */
    private LocalDateTime purgeAt;

    /** 소프트 딜리트 시각 (NULL = 정상) */
    private LocalDateTime deletedAt;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    /** 수정 시각 */
    private LocalDateTime updatedAt;

    private Account(
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

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static Account reconstitute(
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
        return new Account(id, provider, providerUid, email, name, role, status, suspendedUntil, toneMode, lastRoomId, reportBlockedUntil, lastLoginAt, termsAgreedAt, privacyAgreedAt, purgeAt, deletedAt, createdAt, updatedAt);
    }
}
