package com.fiveoutoffive.moa_backend.report.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 신고 (report)
 *
 * 순수 도메인 모델. JPA·Spring 어노테이션을 붙이지 않는다.
 * 생성 팩토리(create)와 상태 변경 메서드는 기능을 개발하면서 도메인 규칙과 함께 추가한다.
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report {

    /** PK */
    private Long id;

    /** 신고자 (NOT NULL) */
    private Long reporterAccountId;

    /** 발생한 방 */
    private Long roomId;

    /** 신고 대상 종류 (NOT NULL) */
    private ReportTargetType targetType;

    /** 신고 대상 ID (NOT NULL) */
    private Long targetId;

    /** 피신고자 (작성자) */
    private Long targetAccountId;

    /** 욕설·비방 / 스팸·광고 / 부적절한 파일 / 개인정보 노출 / 기타 (NOT NULL) */
    private ReportReason reason;

    /** 상세 내용 */
    private String detail;

    /** 신고 시점 대상 내용 (이후 수정·삭제돼도 확인 가능) (NOT NULL) */
    private String snapshotJson;

    /** 접수 / 처리중 / 조치완료 / 반려 (NOT NULL) */
    private ReportStatus status;

    /** 조치: 콘텐츠 삭제 / 계정 정지 / 경고 / 조치 없음 */
    private ReportAction action;

    /** 처리 사유 (조치 없음이어도 필수) */
    private String actionReason;

    /** 허위 신고 판정 여부 (반복되면 신고자의 신고 기능 제한) (NOT NULL) */
    private Boolean isFalseReport;

    /** 처리한 서비스 관리자 */
    private Long handledByAccountId;

    /** 처리 시각 */
    private LocalDateTime handledAt;

    /** 생성 시각 (NOT NULL) */
    private LocalDateTime createdAt;

    /** 수정 시각 */
    private LocalDateTime updatedAt;

    private Report(
            Long id,
            Long reporterAccountId,
            Long roomId,
            ReportTargetType targetType,
            Long targetId,
            Long targetAccountId,
            ReportReason reason,
            String detail,
            String snapshotJson,
            ReportStatus status,
            ReportAction action,
            String actionReason,
            Boolean isFalseReport,
            Long handledByAccountId,
            LocalDateTime handledAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.reporterAccountId = reporterAccountId;
        this.roomId = roomId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.targetAccountId = targetAccountId;
        this.reason = reason;
        this.detail = detail;
        this.snapshotJson = snapshotJson;
        this.status = status;
        this.action = action;
        this.actionReason = actionReason;
        this.isFalseReport = isFalseReport;
        this.handledByAccountId = handledByAccountId;
        this.handledAt = handledAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** 저장소에서 읽은 값으로 복원한다. 검증 없이 그대로 채운다. */
    public static Report reconstitute(
            Long id,
            Long reporterAccountId,
            Long roomId,
            ReportTargetType targetType,
            Long targetId,
            Long targetAccountId,
            ReportReason reason,
            String detail,
            String snapshotJson,
            ReportStatus status,
            ReportAction action,
            String actionReason,
            Boolean isFalseReport,
            Long handledByAccountId,
            LocalDateTime handledAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new Report(id, reporterAccountId, roomId, targetType, targetId, targetAccountId, reason, detail, snapshotJson, status, action, actionReason, isFalseReport, handledByAccountId, handledAt, createdAt, updatedAt);
    }
}
