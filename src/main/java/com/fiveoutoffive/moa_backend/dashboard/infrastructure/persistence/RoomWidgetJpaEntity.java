package com.fiveoutoffive.moa_backend.dashboard.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.dashboard.domain.model.WidgetType;
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

/** 방 대시보드 위젯 배치 (방당 1개 팀 공용, 개인별 배치 없음) (room_widget). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "room_widget")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomWidgetJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Enumerated(EnumType.STRING)
    @Column(name = "widget_type", nullable = false, length = 30)
    private WidgetType widgetType;

    @Column(name = "pos_x", nullable = false)
    private Integer posX;

    @Column(name = "pos_y", nullable = false)
    private Integer posY;

    @Column(name = "width", nullable = false)
    private Integer width;

    @Column(name = "height", nullable = false)
    private Integer height;

    @Column(name = "updated_by_account_id")
    private Long updatedByAccountId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public RoomWidgetJpaEntity(
            Long id,
            Long roomId,
            WidgetType widgetType,
            Integer posX,
            Integer posY,
            Integer width,
            Integer height,
            Long updatedByAccountId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.roomId = roomId;
        this.widgetType = widgetType;
        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;
        this.updatedByAccountId = updatedByAccountId;
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
