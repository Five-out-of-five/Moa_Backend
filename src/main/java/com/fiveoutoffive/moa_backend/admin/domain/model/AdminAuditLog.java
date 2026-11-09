package com.fiveoutoffive.moa_backend.admin.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자 페이지 조작 감사 로그 (1년 보관) (admin_audit_log)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminAuditLog {

    /** PK */
    private Long id;

    /** 조작한 서비스 관리자 (NOT NULL) */
    private Long adminAccountId;

    /** 조작 종류 (회원 정지, 신고 처리, 공지 등록 등) (NOT NULL) */
    private String actionType;

    /** 대상 종류 */
    private String targetType;

    /** 대상 ID */
    private Long targetId;

    /** 변경 전후 등 상세 */
    private String detailJson;

    /** 접속 IP */
    private String ipAddress;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    private AdminAuditLog(
            Long id,
            Long adminAccountId,
            String actionType,
            String targetType,
            Long targetId,
            String detailJson,
            String ipAddress,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.adminAccountId = adminAccountId;
        this.actionType = actionType;
        this.targetType = targetType;
        this.targetId = targetId;
        this.detailJson = detailJson;
        this.ipAddress = ipAddress;
        this.createdAt = createdAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static AdminAuditLog reconstitute(
            Long id,
            Long adminAccountId,
            String actionType,
            String targetType,
            Long targetId,
            String detailJson,
            String ipAddress,
            LocalDateTime createdAt
    ) {
        return new AdminAuditLog(id, adminAccountId, actionType, targetType, targetId, detailJson, ipAddress, createdAt);
    }
}
