package com.fiveoutoffive.moa_backend.ai.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.ai.domain.model.AssistantMessageStatus;
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

/** AI 협업 어시스턴트 대화 이력 (방 단위 보관) (ai_assistant_message). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "ai_assistant_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiAssistantMessageJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "question", nullable = false, length = 200)
    private String question;

    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AssistantMessageStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "reference_json")
    private String referenceJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public AiAssistantMessageJpaEntity(
            Long id,
            Long roomId,
            Long accountId,
            String question,
            String answer,
            AssistantMessageStatus status,
            String referenceJson,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.roomId = roomId;
        this.accountId = accountId;
        this.question = question;
        this.answer = answer;
        this.status = status;
        this.referenceJson = referenceJson;
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
