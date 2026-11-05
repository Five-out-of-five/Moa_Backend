package com.fiveoutoffive.moa_backend.notice.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.notice.domain.model.NoticeType;
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

/** 서비스 공지사항 (notice). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "notice")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "notice_type", nullable = false, length = 20)
    private NoticeType noticeType;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "display_starts_at", nullable = false)
    private LocalDateTime displayStartsAt;

    @Column(name = "display_ends_at", nullable = false)
    private LocalDateTime displayEndsAt;

    @Column(name = "maintenance_starts_at")
    private LocalDateTime maintenanceStartsAt;

    @Column(name = "author_account_id", nullable = false)
    private Long authorAccountId;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public NoticeJpaEntity(
            Long id,
            NoticeType noticeType,
            String title,
            String content,
            LocalDateTime displayStartsAt,
            LocalDateTime displayEndsAt,
            LocalDateTime maintenanceStartsAt,
            Long authorAccountId,
            LocalDateTime deletedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.noticeType = noticeType;
        this.title = title;
        this.content = content;
        this.displayStartsAt = displayStartsAt;
        this.displayEndsAt = displayEndsAt;
        this.maintenanceStartsAt = maintenanceStartsAt;
        this.authorAccountId = authorAccountId;
        this.deletedAt = deletedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // DB 기본값과 같은 값을 채운다. JPA 는 null 을 그대로 INSERT 해서 DB 기본값이 쓰이지 않기 때문이다.
    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (this.createdAt == null) {
            this.createdAt = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
