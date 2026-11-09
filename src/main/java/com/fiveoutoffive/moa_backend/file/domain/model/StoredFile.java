package com.fiveoutoffive.moa_backend.file.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 업로드 파일 메타데이터 (실파일은 외부 스토리지) (file)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoredFile {

    /** PK */
    private Long id;

    /** 업로더 (NOT NULL) */
    private Long ownerAccountId;

    /** 소속 방 (프로필 이미지도 방별이라 모든 파일이 방에 속함). 방 파일 목록·용량 집계 (NOT NULL) */
    private Long roomId;

    /** 파일 용도 (NOT NULL) */
    private FileGroup fileGroup;

    /** 스토리지 객체 키 (NOT NULL) */
    private String objectKey;

    /** 원본 파일명 (NOT NULL) */
    private String originalName;

    /** MIME 타입 */
    private String mimeType;

    /** 크기 (방별 2GB 제한 집계) (NOT NULL) */
    private Long sizeBytes;

    /** 소프트 딜리트 시각 (NULL = 정상) */
    private LocalDateTime deletedAt;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    private StoredFile(
            Long id,
            Long ownerAccountId,
            Long roomId,
            FileGroup fileGroup,
            String objectKey,
            String originalName,
            String mimeType,
            Long sizeBytes,
            LocalDateTime deletedAt,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.ownerAccountId = ownerAccountId;
        this.roomId = roomId;
        this.fileGroup = fileGroup;
        this.objectKey = objectKey;
        this.originalName = originalName;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.deletedAt = deletedAt;
        this.createdAt = createdAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static StoredFile reconstitute(
            Long id,
            Long ownerAccountId,
            Long roomId,
            FileGroup fileGroup,
            String objectKey,
            String originalName,
            String mimeType,
            Long sizeBytes,
            LocalDateTime deletedAt,
            LocalDateTime createdAt
    ) {
        return new StoredFile(id, ownerAccountId, roomId, fileGroup, objectKey, originalName, mimeType, sizeBytes, deletedAt, createdAt);
    }
}
