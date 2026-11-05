package com.fiveoutoffive.moa_backend.chat.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 메시지 첨부 파일 (chat_message_file). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "chat_message_file")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessageFileJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "message_id", nullable = false)
    private Long messageId;

    @Column(name = "file_id", nullable = false)
    private Long fileId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public ChatMessageFileJpaEntity(
            Long id,
            Long messageId,
            Long fileId,
            Integer sortOrder
    ) {
        this.id = id;
        this.messageId = messageId;
        this.fileId = fileId;
        this.sortOrder = sortOrder;
    }
}
