package com.fiveoutoffive.moa_backend.briefing.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.briefing.domain.model.BoardEventType;
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

/** 캐치업 브리핑 '팀 소식' 개별 소거 (방·회원·이벤트 종류별) (briefing_dismissal). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "briefing_dismissal")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BriefingDismissalJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private BoardEventType eventType;

    @Column(name = "dismissed_at", nullable = false)
    private LocalDateTime dismissedAt;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public BriefingDismissalJpaEntity(
            Long id,
            Long roomId,
            Long accountId,
            BoardEventType eventType,
            LocalDateTime dismissedAt
    ) {
        this.id = id;
        this.roomId = roomId;
        this.accountId = accountId;
        this.eventType = eventType;
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
