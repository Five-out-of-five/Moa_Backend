package com.fiveoutoffive.moa_backend.session.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.session.domain.model.SessionStatus;
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

/** 아이디어 보드 회차 (멤버가 직접 시작, 진행 중 회차는 방당 0~1개) (board_session). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "board_session")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardSessionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "session_no", nullable = false)
    private Integer sessionNo;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SessionStatus status;

    @Column(name = "deadline_at")
    private LocalDateTime deadlineAt;

    @Column(name = "started_by_account_id", nullable = false)
    private Long startedByAccountId;

    @Column(name = "closed_by_account_id")
    private Long closedByAccountId;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public BoardSessionJpaEntity(
            Long id,
            Long roomId,
            Integer sessionNo,
            String title,
            SessionStatus status,
            LocalDateTime deadlineAt,
            Long startedByAccountId,
            Long closedByAccountId,
            LocalDateTime startedAt,
            LocalDateTime closedAt,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.roomId = roomId;
        this.sessionNo = sessionNo;
        this.title = title;
        this.status = status;
        this.deadlineAt = deadlineAt;
        this.startedByAccountId = startedByAccountId;
        this.closedByAccountId = closedByAccountId;
        this.startedAt = startedAt;
        this.closedAt = closedAt;
        this.createdAt = createdAt;
    }

    // DB 기본값과 같은 값을 채운다. JPA 는 null 을 그대로 INSERT 해서 DB 기본값이 쓰이지 않기 때문이다.
    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (this.status == null) {
            this.status = SessionStatus.IN_PROGRESS;
        }
        if (this.startedAt == null) {
            this.startedAt = now;
        }
        if (this.createdAt == null) {
            this.createdAt = now;
        }
    }
}
