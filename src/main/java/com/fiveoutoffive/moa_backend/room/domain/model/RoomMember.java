package com.fiveoutoffive.moa_backend.room.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 방 멤버 + 방별 프로필 (room_member)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomMember {

    /** PK */
    private Long id;

    /** 방 (NOT NULL) */
    private Long roomId;

    /** 회원 (NOT NULL) */
    private Long accountId;

    /** 방 권한: 방장 / 멤버 (방 관리 기능은 방장만, 방 안에 관리자 권한 없음) (NOT NULL) */
    private RoomMemberRole role;

    /** 참여 상태 (KICKED 는 재참여 불가) (NOT NULL) */
    private RoomMemberStatus status;

    /** 방별 닉네임 (선택, 2~20자). NULL 이면 기본 프로필 이름(account.name)으로 표시. 직접 정한 닉네임만 방 안에서 중복 불가. 방을 나가면 NULL */
    private String nickname;

    /** 방별 프로필 이미지 (프로필 이미지는 방별로만 존재, NULL 이면 이미지 없이 표시) */
    private Long profileFileId;

    /** 방에서 맡은 역할 (자유 입력 20자) */
    private String positionLabel;

    /** 방 전체 알림 끄기 (NOT NULL) */
    private Boolean notificationMuted;

    /** 캐치업 브리핑 마지막 '모두 확인' 시각 (방별) */
    private LocalDateTime lastBriefingCheckedAt;

    /** 최초 참여 프로필 설정을 마치거나 건너뛴 시각 (NULL 이면 설정 화면 표시, 다시 참여하면 NULL 로 초기화) */
    private LocalDateTime profileSetupAt;

    /** 참여 시각 (NOT NULL) */
    private LocalDateTime joinedAt;

    /** 나간 시각 */
    private LocalDateTime leftAt;

    /** 내보내진 시각 */
    private LocalDateTime kickedAt;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    /** 수정 시각 */
    private LocalDateTime updatedAt;

    private RoomMember(
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

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static RoomMember reconstitute(
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
        return new RoomMember(id, roomId, accountId, role, status, nickname, profileFileId, positionLabel, notificationMuted, lastBriefingCheckedAt, profileSetupAt, joinedAt, leftAt, kickedAt, createdAt, updatedAt);
    }
}
