package com.fiveoutoffive.moa_backend.notice.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 사용자가 닫은 공지 (notice_dismissal). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "notice_dismissal")
@IdClass(NoticeDismissalJpaId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeDismissalJpaEntity {

    @Id
    @Column(name = "notice_id", nullable = false)
    private Long noticeId;

    @Id
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "dismissed_at", nullable = false)
    private LocalDateTime dismissedAt;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public NoticeDismissalJpaEntity(
            Long noticeId,
            Long accountId,
            LocalDateTime dismissedAt
    ) {
        this.noticeId = noticeId;
        this.accountId = accountId;
        this.dismissedAt = dismissedAt;
    }

    // DB 기본값과 같은 값을 채운다. JPA 는 null 을 그대로 INSERT 해서 DB 기본값이 쓰이지 않기 때문이다.
    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (this.dismissedAt == null) {
            this.dismissedAt = now;
        }
    }
}
