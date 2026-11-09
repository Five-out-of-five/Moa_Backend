package com.fiveoutoffive.moa_backend.room.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 방 (프로젝트 조직 단위 워크스페이스) (room)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room {

    /** PK */
    private Long id;

    /** 방 이름 (2~30자) (NOT NULL) */
    private String name;

    /** 방 설명 */
    private String description;

    /** 대표 이미지 */
    private Long imageFileId;

    /** 방 고유 링크 코드 (초대 링크 = 이 코드의 공유 형태, 유효기간 없음. 링크로 들어오면 항상 참여 요청 → 방장 수락) (NOT NULL) */
    private String inviteCode;

    /** 방 상태 (서비스 관리자 비활성 처리) (NOT NULL) */
    private RoomStatus status;

    /** 비활성 사유 */
    private String inactiveReason;

    /** 비활성 처리 시각 */
    private LocalDateTime inactivatedAt;

    /** 비활성 처리한 관리자 */
    private Long inactivatedByAccountId;

    /** 개설자 (1인당 개설 수 제한 집계 기준) (NOT NULL) */
    private Long createdByAccountId;

    /** 대시보드 배치 버전. 배치가 바뀔 때마다 증가시키고 전체 배치를 방에 브로드캐스트 (동시 편집 시 화면 정합성 기준) (NOT NULL) */
    private Long layoutVersion;

    /** 최근 활동 시각 (관리자 방 목록) */
    private LocalDateTime lastActivityAt;

    /** 소프트 딜리트 시각 (NULL = 정상) */
    private LocalDateTime deletedAt;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    /** 수정 시각 */
    private LocalDateTime updatedAt;

    private Room(
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

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static Room reconstitute(
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
        return new Room(id, name, description, imageFileId, inviteCode, status, inactiveReason, inactivatedAt, inactivatedByAccountId, createdByAccountId, layoutVersion, lastActivityAt, deletedAt, createdAt, updatedAt);
    }
}
