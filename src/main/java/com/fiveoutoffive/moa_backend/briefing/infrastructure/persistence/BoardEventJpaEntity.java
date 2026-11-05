package com.fiveoutoffive.moa_backend.briefing.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.briefing.domain.model.BoardEventTargetType;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** 방 변경 이벤트 로그 (캐치업 브리핑 원천, 30일 보관) (board_event). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "board_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardEventJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private BoardEventType eventType;

    @Column(name = "actor_account_id")
    private Long actorAccountId;

    @Column(name = "recipient_account_id")
    private Long recipientAccountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private BoardEventTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public BoardEventJpaEntity(
            Long id,
            Long roomId,
            BoardEventType eventType,
            Long actorAccountId,
            Long recipientAccountId,
            BoardEventTargetType targetType,
            Long targetId,
            String payload,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.roomId = roomId;
        this.eventType = eventType;
        this.actorAccountId = actorAccountId;
        this.recipientAccountId = recipientAccountId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.payload = payload;
        this.createdAt = createdAt;
    }

    // DB 기본값과 같은 값을 채운다. JPA 는 null 을 그대로 INSERT 해서 DB 기본값이 쓰이지 않기 때문이다.
    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (this.createdAt == null) {
            this.createdAt = now;
        }
    }
}
